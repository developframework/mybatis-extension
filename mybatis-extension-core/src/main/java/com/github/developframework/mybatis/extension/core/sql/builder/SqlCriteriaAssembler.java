package com.github.developframework.mybatis.extension.core.sql.builder;

import com.github.developframework.mybatis.extension.core.sql.SqlCriteria;

/**
 * @author qiushui on 2023-09-15.
 */
@FunctionalInterface
public interface SqlCriteriaAssembler {

    SqlCriteria assemble(SqlRoot root, SqlCriteriaBuilder builder);
}
