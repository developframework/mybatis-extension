package com.github.developframework.mybatis.extension.core.sql;

import lombok.RequiredArgsConstructor;

/**
 * @author qiushui on 2023-09-15.
 */
@RequiredArgsConstructor
public class SqlFunction implements SqlFieldPart {

    private final String function;

    private final String[] args;

    @Override
    public String toSql() {
        return String.format("%s(%s)", function.toUpperCase(), String.join(", ", args));
    }
}
