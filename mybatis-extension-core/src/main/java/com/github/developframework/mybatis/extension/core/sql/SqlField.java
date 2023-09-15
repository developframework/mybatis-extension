package com.github.developframework.mybatis.extension.core.sql;

import com.github.developframework.mybatis.extension.core.utils.NameUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author qiushui on 2023-09-15.
 */
@Getter
@RequiredArgsConstructor
public class SqlField implements SqlFieldPart {

    private final String fieldName;

    private final String tableAlias;

    private final String as;

    @Override
    public String toSql() {
        String sql;
        if (tableAlias == null) {
            sql = NameUtils.wrap(fieldName);
        } else {
            sql = NameUtils.wrap(tableAlias) + "." + NameUtils.wrap(fieldName);
        }
        if (as != null) {
            sql += " AS " + as;
        }
        return sql;
    }
}
