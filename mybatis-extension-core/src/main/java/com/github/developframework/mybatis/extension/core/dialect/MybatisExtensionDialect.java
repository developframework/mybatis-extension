package com.github.developframework.mybatis.extension.core.dialect;

import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;

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

    /**
     * sql片段构建器
     */
    SqlSourceBuilder sqlFragmentBuilder();

    /**
     * 构建字段描述
     */
    ColumnDescription buildColumnDescriptionByColumnDefinition(ColumnDefinition columnDefinition);
}
