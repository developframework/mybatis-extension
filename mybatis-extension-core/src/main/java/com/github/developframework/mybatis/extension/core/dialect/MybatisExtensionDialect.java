package com.github.developframework.mybatis.extension.core.dialect;

/**
 * @author qiushui on 2024-12-19.
 */
public interface MybatisExtensionDialect {

    /**
     * 表名
     */
    String tableName(String originalTableName);

    /**
     * 字段名
     */
    String columnName(String originalColumnName);

    /**
     * 常量
     */
    String literal(String value);
}
