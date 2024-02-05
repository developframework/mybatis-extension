package com.github.developframework.mybatis.extension.core.sql.builder;

import com.github.developframework.mybatis.extension.core.sql.SqlCriteria;

/**
 * Sql判断装配器
 *
 * @author qiushui on 2023-09-15.
 */
@FunctionalInterface
public interface SqlCriteriaAssembler {

    SqlCriteria assemble(SqlRoot root, SqlCriteriaBuilder builder);
}
