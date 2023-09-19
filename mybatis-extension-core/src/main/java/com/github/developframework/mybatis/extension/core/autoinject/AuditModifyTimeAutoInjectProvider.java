package com.github.developframework.mybatis.extension.core.autoinject;

import org.apache.ibatis.mapping.SqlCommandType;

/**
 * @author qiushui on 2023-09-18.
 */
public class AuditModifyTimeAutoInjectProvider extends TimeAutoInjectProvider {

    @Override
    public SqlCommandType[] needInject() {
        return new SqlCommandType[]{SqlCommandType.INSERT, SqlCommandType.UPDATE};
    }
}
