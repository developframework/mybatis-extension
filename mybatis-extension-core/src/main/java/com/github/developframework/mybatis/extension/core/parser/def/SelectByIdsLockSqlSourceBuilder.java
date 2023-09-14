package com.github.developframework.mybatis.extension.core.parser.def;

/**
 * @author qiushui on 2023-09-14.
 */
public class SelectByIdsLockSqlSourceBuilder extends SelectByIdArrayLockSqlSourceBuilder {

    @Override
    public String methedName() {
        return "selectByIdsLock";
    }
}
