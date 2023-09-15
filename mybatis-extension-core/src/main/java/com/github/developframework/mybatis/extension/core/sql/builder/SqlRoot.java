package com.github.developframework.mybatis.extension.core.sql.builder;

import com.github.developframework.mybatis.extension.core.sql.SqlField;
import com.github.developframework.mybatis.extension.core.sql.SqlFieldPart;
import com.github.developframework.mybatis.extension.core.sql.SqlFunction;
import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.utils.NameUtils;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * @author qiushui on 2023-09-15.
 */
@RequiredArgsConstructor
public class SqlRoot {

    private final EntityDefinition entityDefinition;

    public SqlFieldPart get(String property) {
        final ColumnDefinition columnDefinition = entityDefinition.getColumnDefinition(property);
        return new SqlField(columnDefinition.getColumn(), null, null);
    }

    public SqlFieldPart function(String function, Object... args) {
        return new SqlFunction(
                function,
                Arrays.stream(args)
                        .map(arg -> {
                            if (arg instanceof String property) {
                                final ColumnDefinition columnDefinition = entityDefinition.getColumnDefinitions().get(property);
                                if (columnDefinition != null) {
                                    return columnDefinition.wrapColumn();
                                } else {
                                    return NameUtils.literal(property);
                                }
                            } else {
                                return arg.toString();
                            }
                        })
                        .toArray(String[]::new)
        );
    }
}
