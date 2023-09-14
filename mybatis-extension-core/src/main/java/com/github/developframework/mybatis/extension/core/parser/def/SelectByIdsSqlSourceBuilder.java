package com.github.developframework.mybatis.extension.core.parser.def;

/**
 * @author qiushui on 2023-09-08.
 */
public class SelectByIdsSqlSourceBuilder extends SelectByIdArraySqlSourceBuilder {
    @Override
    public String methedName() {
        return "selectByIds";
    }
}
