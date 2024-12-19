package com.github.developframework.mybatis.extension.dialect.mysql;

import com.github.developframework.mybatis.extension.core.dialect.MybatisExtensionDialect;

/**
 * @author qiushui on 2024-12-19.
 */
public class MysqlMybatisExtensionDialect implements MybatisExtensionDialect {

    @Override
    public String tableName(String originalTableName) {
        return "`" + originalTableName + "`";
    }

    @Override
    public String columnName(String originalColumnName) {
        return "`" + originalColumnName + "`";
    }

    @Override
    public String literal(String value) {
        return "'" + value + "'";
    }
}
