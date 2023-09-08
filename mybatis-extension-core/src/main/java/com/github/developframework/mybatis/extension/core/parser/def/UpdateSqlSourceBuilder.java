package com.github.developframework.mybatis.extension.core.parser.def;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MapperMethodParseWrapper;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author qiushui on 2023-09-08.
 */
public class UpdateSqlSourceBuilder extends AbstractSqlSourceBuilder {

    @Override
    public String methedName() {
        return "update";
    }

    @Override
    public MapperMethodParseWrapper build(Configuration configuration, EntityDefinition entityDefinition, Method method) {
        final List<SqlNode> setSqlNodes = new ArrayList<>();
        final ColumnDefinition[] idColumnDefinitions = entityDefinition.getPrimaryKeyColumnDefinitions();

        String whereSql;
        if (entityDefinition.isCompositeId()) {
            whereSql = "WHERE " + Stream.of(idColumnDefinitions)
                    .map(cd -> String.format("%s = %s", cd.wrapColumn(), compositeIdPlaceholder(cd)))
                    .collect(Collectors.joining(Interval.AND.getText()));
        } else {
            whereSql = String.format("WHERE %s = %s", idColumnDefinitions[0].wrapColumn(), idColumnDefinitions[0].placeholder());
        }

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
            if (columnDefinition.isPrimaryKey() || columnDefinition.isVersion()) {
                continue;
            }
            if (!columnDefinition.isMultipleTenant()) {
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
        }
        final List<SqlNode> sqlNodes = new LinkedList<>();
        sqlNodes.add(new StaticTextSqlNode("UPDATE " + entityDefinition.wrapTableName()));
        sqlNodes.add(new SetSqlNode(configuration, new MixedSqlNode(setSqlNodes)));
        sqlNodes.add(new StaticTextSqlNode(whereSql));
        if (entityDefinition.hasMultipleTenant()) {
            sqlNodes.add(multipleTenantSqlNodes(entityDefinition));
        }
        SqlSource sqlSource = new DynamicSqlSource(configuration, new MixedSqlNode(sqlNodes));
        return new MapperMethodParseWrapper(SqlCommandType.UPDATE, sqlSource);
    }
}
