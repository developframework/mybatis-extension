package com.github.developframework.mybatis.extension.dialect.mysql;

import com.github.developframework.mybatis.extension.core.dialect.BaseSqlSourceBuilder;
import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.utils.MybatisUtils;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author qiushui on 2024-12-20.
 */
public class MysqlSqlSourceBuilder extends BaseSqlSourceBuilder {

    public static final MysqlSqlSourceBuilder INSTANCE = new MysqlSqlSourceBuilder();

    /**
     * 创建表语句
     */
    @Override
    public SqlSource buildCreateTableSql(Configuration configuration, EntityDefinition entityDefinition) {
        final String sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(entityDefinition.wrapTableName())
                .append(" (")
                .append(
                        entityDefinition.getColumnDefinitions()
                                .values()
                                .stream()
                                .map(cd -> entityDefinition.getDialect().buildColumnDescriptionByColumnDefinition(cd).toString())
                                .collect(Collectors.joining(","))
                )
                .append(", PRIMARY KEY (")
                .append(
                        Arrays.stream(entityDefinition.getPrimaryKeyColumnDefinitions())
                                .map(ColumnDefinition::getColumn)
                                .collect(Collectors.joining(","))
                )
                .append(")")
                .append(") ENGINE=").append(entityDefinition.getEngine().name())
                .append(entityDefinition.getComment().isEmpty() ? "" : (String.format(" COMMENT = '%s'", entityDefinition.getComment())))
                .toString();
        return new RawSqlSource(configuration, sql, null);
    }

    /**
     * 修改表语句
     */
    @Override
    public SqlSource buildAlterTableSql(Configuration configuration, EntityDefinition entityDefinition) {
        StaticTextSqlNode staticTextSqlNode = new StaticTextSqlNode("ALTER TABLE " + entityDefinition.wrapTableName() + " ");
        final ForEachSqlNode forEachSqlNode = new ForEachSqlNode(
                configuration,
                new TextSqlNode("${it}"),
                "collection",
                false,
                null,
                "it",
                null,
                null,
                ","
        );
        return new DynamicSqlSource(configuration, new MixedSqlNode(List.of(staticTextSqlNode, forEachSqlNode)));
    }

    /**
     * 删除语句
     */
    @Override
    public SqlSource buildDeleteByIdSql(Configuration configuration, EntityDefinition entityDefinition) {
        if (entityDefinition.hasLogicDelete()) {
            return buildUpdateSqlSource(configuration, entityDefinition);
        } else {
            return buildDeleteSqlSource(configuration, entityDefinition);
        }
    }

    private SqlSource buildUpdateSqlSource(Configuration configuration, EntityDefinition entityDefinition) {
        final String whereSql = buildWhereByIdSql(entityDefinition);
        final StaticTextSqlNode setContentSqlNode = new StaticTextSqlNode(String.format("%s = 1", entityDefinition.getLogicDeleteColumnDefinition().wrapColumn()));
        final List<SqlNode> sqlNodes = new LinkedList<>();
        sqlNodes.add(new StaticTextSqlNode("UPDATE " + entityDefinition.wrapTableName()));
        sqlNodes.add(new SetSqlNode(configuration, setContentSqlNode));
        sqlNodes.add(new StaticTextSqlNode(whereSql));
        if (entityDefinition.hasMultipleTenant()) {
            sqlNodes.addAll(multipleTenantSqlNodes(entityDefinition));
        }
        return new DynamicSqlSource(configuration, new MixedSqlNode(sqlNodes));
    }

    private SqlSource buildDeleteSqlSource(Configuration configuration, EntityDefinition entityDefinition) {
        final String sql = "DELETE FROM " + entityDefinition.wrapTableName() + buildWhereByIdSql(entityDefinition);
        if (entityDefinition.hasMultipleTenant()) {
            final List<SqlNode> sqlNodes = new LinkedList<>();
            sqlNodes.add(new StaticTextSqlNode(sql));
            sqlNodes.addAll(multipleTenantSqlNodes(entityDefinition));
            final MixedSqlNode mixedSqlNode = new MixedSqlNode(sqlNodes);
            return new DynamicSqlSource(configuration, mixedSqlNode);
        } else {
            return new RawSqlSource(configuration, sql, entityDefinition.getEntityClass());
        }
    }

    /**
     * 查询字段语句
     */
    @Override
    public SqlSource buildDescSql(Configuration configuration, EntityDefinition entityDefinition) {
        final String sql = "DESC " + entityDefinition.wrapTableName();
        return new RawSqlSource(configuration, sql, null);
    }

    /**
     * 查询存在语句
     */
    @Override
    public SqlSource buildExistsByIdSql(Configuration configuration, EntityDefinition entityDefinition) {
        String sql = "SELECT 1 FROM " + entityDefinition.wrapTableName() + buildWhereByIdSql(entityDefinition);
        if (entityDefinition.hasMultipleTenant()) {
            final List<SqlNode> sqlNodes = new LinkedList<>();
            sqlNodes.add(new StaticTextSqlNode("SELECT IFNULL(("));
            sqlNodes.add(new StaticTextSqlNode(sql));
            sqlNodes.addAll(multipleTenantSqlNodes(entityDefinition));
            sqlNodes.add(new StaticTextSqlNode("LIMIT 1), 0) `exists`"));
            return new DynamicSqlSource(configuration, new MixedSqlNode(sqlNodes));
        } else {
            sql = String.format("SELECT IFNULL((%s LIMIT 1), 0) `exists`", sql);
            return new RawSqlSource(configuration, sql, entityDefinition.getEntityClass());
        }
    }

    /**
     * 批量插入语句
     */
    @Override
    public SqlSource buildInsertAllSql(Configuration configuration, EntityDefinition entityDefinition, Method method) {
        StringBuilder fields = new StringBuilder(" (");
        StringBuilder values = new StringBuilder(" (");
        List<ColumnDefinition> columnDefinitions = new ArrayList<>(entityDefinition.getColumnDefinitions().values());

        for (int i = 0; i < columnDefinitions.size(); i++) {
            final ColumnDefinition columnDefinition = columnDefinitions.get(i);
            if (!columnDefinition.isAutoIncrement()) {
                fields.append(columnDefinition.wrapColumn());
                values.append(columnDefinition.getColumnMybatisPlaceholder().placeholder("it." + columnDefinition.getProperty()));
                if (i < columnDefinitions.size() - 1) {
                    fields.append(",");
                    values.append(",");
                } else {
                    fields.append(")");
                    values.append(")");
                }
            }
        }

        StringBuilder sb = new StringBuilder("INSERT ");
//        if (entityDefinition.isInsertIgnore()) {
//            sb.append("IGNORE ");
//        }
        sb.append("INTO ").append(entityDefinition.wrapTableName());
        sb.append(fields);
        sb.append(" VALUES");

        final ForEachSqlNode forEachSqlNode = new ForEachSqlNode(
                configuration,
                new StaticTextSqlNode(values.toString()),
                MybatisUtils.getCollectionExpression(method, null),
                false,
                null,
                "it",
                null,
                null,
                ","
        );
        final MixedSqlNode mixedSqlNode = new MixedSqlNode(List.of(new StaticTextSqlNode(sb.toString()), forEachSqlNode));
        return new DynamicSqlSource(configuration, mixedSqlNode);
    }

    @Override
    public SqlSource buildInsertSql(Configuration configuration, EntityDefinition entityDefinition, Method method) {
        StringBuilder fields = new StringBuilder(" (");
        StringBuilder values = new StringBuilder(" (");
        List<ColumnDefinition> columnDefinitions = new ArrayList<>(entityDefinition.getColumnDefinitions().values());

        for (int i = 0; i < columnDefinitions.size(); i++) {
            final ColumnDefinition columnDefinition = columnDefinitions.get(i);
            if (!columnDefinition.isAutoIncrement()) {
                fields.append(columnDefinition.wrapColumn());
                values.append(columnDefinition.placeholder());
                if (i < columnDefinitions.size() - 1) {
                    fields.append(",");
                    values.append(",");
                } else {
                    fields.append(")");
                    values.append(")");
                }
            }
        }
        StringBuilder sb = new StringBuilder("INSERT ");
//        if (entityDefinition.isInsertIgnore()) {
//            sb.append("IGNORE ");
//        }
        sb.append("INTO ")
                .append(entityDefinition.wrapTableName())
                .append(fields)
                .append(" VALUES")
                .append(values);
        return new RawSqlSource(configuration, sb.toString(), entityDefinition.getEntityClass());
    }

    @Override
    public SqlSource buildReplaceAllSql(Configuration configuration, EntityDefinition entityDefinition, Method method) {
        StringBuilder fields = new StringBuilder(" (");
        StringBuilder values = new StringBuilder(" (");
        List<ColumnDefinition> columnDefinitions = new ArrayList<>(entityDefinition.getColumnDefinitions().values());

        for (int i = 0; i < columnDefinitions.size(); i++) {
            final ColumnDefinition columnDefinition = columnDefinitions.get(i);
            if (!columnDefinition.isAutoIncrement()) {
                fields.append(columnDefinition.wrapColumn());
                values.append(columnDefinition.getColumnMybatisPlaceholder().placeholder("it." + columnDefinition.getProperty()));
                if (i < columnDefinitions.size() - 1) {
                    fields.append(",");
                    values.append(",");
                } else {
                    fields.append(")");
                    values.append(")");
                }
            }
        }

        StringBuilder sb = new StringBuilder("REPLACE ");
//        if (entityDefinition.isInsertIgnore()) {
//            sb.append("IGNORE ");
//        }
        sb.append("INTO ").append(entityDefinition.wrapTableName());
        sb.append(fields);
        sb.append(" VALUES");

        final ForEachSqlNode forEachSqlNode = new ForEachSqlNode(
                configuration,
                new StaticTextSqlNode(values.toString()),
                MybatisUtils.getCollectionExpression(method, null),
                false,
                null,
                "it",
                null,
                null,
                ","
        );
        final MixedSqlNode mixedSqlNode = new MixedSqlNode(List.of(new StaticTextSqlNode(sb.toString()), forEachSqlNode));
        return new DynamicSqlSource(configuration, mixedSqlNode);
    }

    @Override
    public SqlSource buildReplaceSql(Configuration configuration, EntityDefinition entityDefinition, Method method) {
        StringBuilder fields = new StringBuilder(" (");
        StringBuilder values = new StringBuilder(" (");
        List<ColumnDefinition> columnDefinitions = new ArrayList<>(entityDefinition.getColumnDefinitions().values());

        for (int i = 0; i < columnDefinitions.size(); i++) {
            final ColumnDefinition columnDefinition = columnDefinitions.get(i);
            if (!columnDefinition.isAutoIncrement()) {
                fields.append(columnDefinition.wrapColumn());
                values.append(columnDefinition.placeholder());
                if (i < columnDefinitions.size() - 1) {
                    fields.append(",");
                    values.append(",");
                } else {
                    fields.append(")");
                    values.append(")");
                }
            }
        }
        StringBuilder sb = new StringBuilder("REPLACE ");
//        if (entityDefinition.isInsertIgnore()) {
//            sb.append("IGNORE ");
//        }
        sb.append("INTO ")
                .append(entityDefinition.wrapTableName())
                .append(fields)
                .append(" VALUES")
                .append(values);
        return new RawSqlSource(configuration, sb.toString(), entityDefinition.getEntityClass());
    }

    @Override
    public SqlSource buildSelectAllSql(Configuration configuration, EntityDefinition entityDefinition, Method method) {
        final List<SqlNode> sqlNodes = new LinkedList<>();
        if (entityDefinition.hasLogicDelete()) {
            sqlNodes.add(new StaticTextSqlNode(String.format("%s = 0", entityDefinition.getLogicDeleteColumnDefinition().wrapColumn())));
        }
        if (entityDefinition.hasMultipleTenant()) {
            sqlNodes.addAll(multipleTenantSqlNodes(entityDefinition));
        }
        return new DynamicSqlSource(configuration, new MixedSqlNode(
                List.of(
                        new StaticTextSqlNode("SELECT * FROM " + entityDefinition.wrapTableName()),
                        new WhereSqlNode(configuration, new MixedSqlNode(sqlNodes))
                )
        ));
    }

    @Override
    public SqlSource buildSelectByIdArraySql(Configuration configuration, EntityDefinition entityDefinition, Method method, boolean lock) {
        final ColumnDefinition[] idColumnDefinitions = entityDefinition.getPrimaryKeyColumnDefinitions();
        final List<SqlNode> sqlNodes = new LinkedList<>();
        String sql;
        final String forEachItemSql;
        if (entityDefinition.isCompositeId()) {
            // 多字段 IN 操作
            sql = new StringBuilder("SELECT * FROM ")
                    .append(entityDefinition.wrapTableName())
                    .append(" WHERE ")
                    .append(
                            Stream
                                    .of(idColumnDefinitions)
                                    .map(ColumnDefinition::wrapColumn)
                                    .collect(Collectors.joining(", ", "(", ")"))
                    )
                    .append(" IN ")
                    .toString();
            forEachItemSql = Stream
                    .of(idColumnDefinitions)
                    .map(cd -> String.format("#{id.innerMap.%s}", cd.getProperty()))
                    .collect(Collectors.joining(", ", "(", ")"));
        } else {
            // 单字段 IN 操作
            sql = String.format(
                    "SELECT * FROM %s WHERE %s IN",
                    entityDefinition.wrapTableName(),
                    idColumnDefinitions[0].wrapColumn()
            );
            forEachItemSql = "#{id}";
        }
        if (entityDefinition.hasLogicDelete()) {
            sql += String.format(" AND %s = 0", entityDefinition.getLogicDeleteColumnDefinition().wrapColumn());
        }
        sqlNodes.add(new StaticTextSqlNode(sql));
        sqlNodes.add(
                new ForEachSqlNode(
                        configuration,
                        new StaticTextSqlNode(forEachItemSql),
                        MybatisUtils.getCollectionExpression(method, ParamNameResolver.GENERIC_NAME_PREFIX + 1),
                        false,
                        null,
                        "id",
                        "(",
                        ")",
                        ","
                )
        );
        if (entityDefinition.hasMultipleTenant()) {
            sqlNodes.addAll(multipleTenantSqlNodes(entityDefinition));
        }
        if (lock) {
            sqlNodes.add(lockChooseSqlNode());
        }
        return new DynamicSqlSource(configuration, new MixedSqlNode(sqlNodes));
    }

    @Override
    public SqlSource buildSelectByIdSql(Configuration configuration, EntityDefinition entityDefinition, Method method, boolean lock) {
        final String sql = "SELECT * FROM " + entityDefinition.wrapTableName() + buildWhereByIdSql(entityDefinition);
        final List<SqlNode> sqlNodes = new LinkedList<>();
        sqlNodes.add(new StaticTextSqlNode(sql));
        if (entityDefinition.hasMultipleTenant()) {
            sqlNodes.addAll(multipleTenantSqlNodes(entityDefinition));
        }
        sqlNodes.add(new StaticTextSqlNode(" LIMIT 1"));
        if (lock) {
            sqlNodes.add(lockChooseSqlNode());
        }
        return new DynamicSqlSource(configuration, new MixedSqlNode(sqlNodes));
    }

    @Override
    public SqlSource buildShowIndexSql(Configuration configuration, EntityDefinition entityDefinition, Method method) {
        final String sql = "SHOW INDEX FROM " + entityDefinition.wrapTableName() + "WHERE Key_name != 'PRIMARY'";
        return new RawSqlSource(configuration, sql, null);
    }

    @Override
    public SqlSource buildUpdateSql(Configuration configuration, EntityDefinition entityDefinition, Method method) {
        final List<SqlNode> setSqlNodes = new ArrayList<>();
        String whereSql = buildWhereByIdSql(entityDefinition);

        if (entityDefinition.hasOptimisticLock()) {
            final ColumnDefinition versionColumnDefinition = entityDefinition.getVersionColumnDefinition();
            // 乐观锁修改
            if (versionColumnDefinition != null) {
                // 拼接@Version字段
                whereSql += String.format(" AND %s = %s", versionColumnDefinition.wrapColumn(), versionColumnDefinition.placeholder());
                // set 语句需要自增@Version字段
                final String nodeStr = String.format("%s = %s + 1", versionColumnDefinition.wrapColumn(), versionColumnDefinition.wrapColumn());
                setSqlNodes.add(new StaticTextSqlNode(nodeStr));
            }
        }

        for (ColumnDefinition columnDefinition : entityDefinition.getColumnDefinitions().values()) {
            if (columnDefinition.isPrimaryKey() || columnDefinition.isVersion() || columnDefinition.isMultipleTenant() || columnDefinition.isLogicDelete()) {
                continue;
            }
            final String nodeStr = String.format(", %s = %s", columnDefinition.wrapColumn(), columnDefinition.placeholder());
            final StaticTextSqlNode textSqlNode = new StaticTextSqlNode(nodeStr);
            final SqlNode sqlNode;
            if (columnDefinition.getColumnBuildMetadata().nullable) {
                sqlNode = textSqlNode;
            } else {
                sqlNode = new IfSqlNode(textSqlNode, columnDefinition.getProperty() + " neq null");
            }
            setSqlNodes.add(sqlNode);
        }
        final List<SqlNode> sqlNodes = new LinkedList<>();
        sqlNodes.add(new StaticTextSqlNode("UPDATE " + entityDefinition.wrapTableName()));
        sqlNodes.add(new SetSqlNode(configuration, new MixedSqlNode(setSqlNodes)));
        sqlNodes.add(new StaticTextSqlNode(whereSql));
        if (entityDefinition.hasMultipleTenant()) {
            sqlNodes.addAll(multipleTenantSqlNodes(entityDefinition));
        }
        return new DynamicSqlSource(configuration, new MixedSqlNode(sqlNodes));
    }
}
