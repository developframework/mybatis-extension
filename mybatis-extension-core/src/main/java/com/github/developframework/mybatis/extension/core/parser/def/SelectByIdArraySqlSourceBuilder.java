package com.github.developframework.mybatis.extension.core.parser.def;

import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MapperMethodParseWrapper;
import com.github.developframework.mybatis.extension.core.utils.MybatisUtils;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author qiushui on 2023-09-08.
 */
public class SelectByIdArraySqlSourceBuilder extends AbstractSqlSourceBuilder {
    @Override
    public String methedName() {
        return "selectByIdArray";
    }

    @Override
    public MapperMethodParseWrapper build(Configuration configuration, EntityDefinition entityDefinition, Method method) {
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
        SqlSource sqlSource = new DynamicSqlSource(configuration, new MixedSqlNode(sqlNodes));
        return new MapperMethodParseWrapper(SqlCommandType.SELECT, sqlSource);
    }
}
