package com.github.developframework.mybatis.extension.core.parser.def;

/**
 * @author qiushui on 2023-09-08.
 */
public class SelectByIdsSqlParseHandler extends SelectByIdArraySqlParseHandler {
    @Override
    public String methodName() {
        return "selectByIds";
    }
}
