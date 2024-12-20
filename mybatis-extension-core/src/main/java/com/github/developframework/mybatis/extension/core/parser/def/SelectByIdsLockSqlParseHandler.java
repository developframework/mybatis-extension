package com.github.developframework.mybatis.extension.core.parser.def;

/**
 * @author qiushui on 2023-09-14.
 */
public class SelectByIdsLockSqlParseHandler extends SelectByIdArrayLockSqlParseHandler {

    @Override
    public String methodName() {
        return "selectByIdsLock";
    }
}
