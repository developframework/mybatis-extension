package com.github.developframework.mybatis.extension.core.parser.def;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.ParameterKeys;
import org.apache.ibatis.scripting.xmltags.IfSqlNode;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author qiushui on 2023-09-06.
 */
public abstract class AbstractSqlSourceBuilder implements SqlSourceBuilder {

    protected final String buildSql(EntityDefinition entityDefinition, String sqlPrefix) {
        final ColumnDefinition[] idColumnDefinitions = entityDefinition.getPrimaryKeyColumnDefinitions();
        if (entityDefinition.isCompositeId()) {
            return sqlPrefix + " FROM " + entityDefinition.wrapTableName() + " WHERE " + Stream.of(idColumnDefinitions)
                    .map(cd -> String.format("%s = %s", cd.wrapColumn(), compositeIdPlaceholder(cd)))
                    .collect(Collectors.joining(Interval.AND.getText()));
        } else {
            return String.format(
                    sqlPrefix + " FROM %s WHERE %s = %s",
                    entityDefinition.wrapTableName(),
                    idColumnDefinitions[0].wrapColumn(),
                    idColumnDefinitions[0].placeholder()
            );
        }
    }

    protected final String compositeIdPlaceholder(ColumnDefinition columnDefinition) {
        return columnDefinition.getColumnMybatisPlaceholder().placeholder(ParameterKeys.ID + "." + columnDefinition.getProperty());
    }

    protected final MixedSqlNode multipleTenantSqlNodes(EntityDefinition entityDefinition) {
        final ColumnDefinition[] multipleTenantColumnDefinitions = entityDefinition.getMultipleTenantColumnDefinitions();
        final List<SqlNode> ifSqlNodes = Arrays.stream(multipleTenantColumnDefinitions)
                .map(cd -> {
                    final StaticTextSqlNode textSqlNode = new StaticTextSqlNode(
                            String.format("AND %s = %s", cd.wrapColumn(), cd.placeholder())
                    );
                    return new IfSqlNode(textSqlNode, cd.getProperty() + " neq null");
                })
                .collect(Collectors.toList());
        return new MixedSqlNode(ifSqlNodes);
    }
}
