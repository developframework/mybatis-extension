package com.github.developframework.mybatis.extension.core.dialect;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.LockType;
import com.github.developframework.mybatis.extension.core.structs.ParameterKeys;
import org.apache.ibatis.scripting.xmltags.ChooseSqlNode;
import org.apache.ibatis.scripting.xmltags.IfSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author qiushui on 2024-12-20.
 */
public abstract class BaseSqlSourceBuilder implements SqlSourceBuilder {

    protected final String buildWhereByIdSql(EntityDefinition entityDefinition) {
        final ColumnDefinition[] idColumnDefinitions = entityDefinition.getPrimaryKeyColumnDefinitions();
        String whereSql;
        if (entityDefinition.isCompositeId()) {
            whereSql = " WHERE " + Stream.of(idColumnDefinitions)
                    .map(cd -> String.format("%s = %s", cd.wrapColumn(), compositeIdPlaceholder(cd)))
                    .collect(Collectors.joining(Interval.AND.getText()));
        } else {
            whereSql = String.format(" WHERE %s = %s", idColumnDefinitions[0].wrapColumn(), idColumnDefinitions[0].placeholder());
        }

        if (entityDefinition.hasLogicDelete()) {
            final ColumnDefinition logicDeleteColumnDefinition = entityDefinition.getLogicDeleteColumnDefinition();
            if (logicDeleteColumnDefinition != null) {
                // 拼接@LogicDelete字段
                whereSql += String.format(" AND %s = 0", logicDeleteColumnDefinition.wrapColumn());
            }
        }
        return whereSql;
    }

    protected final String compositeIdPlaceholder(ColumnDefinition columnDefinition) {
        return columnDefinition.getColumnMybatisPlaceholder().placeholder(ParameterKeys.ID + "." + columnDefinition.getProperty());
    }

    protected final List<SqlNode> multipleTenantSqlNodes(EntityDefinition entityDefinition) {
        final ColumnDefinition[] multipleTenantColumnDefinitions = entityDefinition.getMultipleTenantColumnDefinitions();
        return Arrays.stream(multipleTenantColumnDefinitions)
                .map(cd -> {
                    final StaticTextSqlNode textSqlNode = new StaticTextSqlNode(
                            String.format(" AND %s = %s", cd.wrapColumn(), cd.placeholder())
                    );
                    return new IfSqlNode(textSqlNode, cd.getProperty() + " neq null");
                })
                .collect(Collectors.toList());
    }

    protected final ChooseSqlNode lockChooseSqlNode() {
        return new ChooseSqlNode(
                List.of(
                        new IfSqlNode(
                                new StaticTextSqlNode(LockType.WRITE.getSql()),
                                String.format("%s eq @%s@%s", ParameterKeys.LOCK, LockType.class.getName(), LockType.WRITE.name())
                        ),
                        new IfSqlNode(
                                new StaticTextSqlNode(LockType.READ.getSql()),
                                String.format("%s eq @%s@%s", ParameterKeys.LOCK, LockType.class.getName(), LockType.READ.name())
                        )
                ),
                new StaticTextSqlNode("")
        );
    }
}
