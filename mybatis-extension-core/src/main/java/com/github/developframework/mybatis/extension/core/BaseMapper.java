package com.github.developframework.mybatis.extension.core;

import com.github.developframework.mybatis.extension.core.sql.SqlSortPart;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaAssembler;
import com.github.developframework.mybatis.extension.core.structs.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author qiushui on 2023-08-30.
 * @see com.github.developframework.mybatis.extension.core.parser.def.BaseMapperDefaultParser
 */
public interface BaseMapper<T, ID extends Serializable> {

    String AUTOMATIC_SQL = "";

    /**
     * @see com.github.developframework.mybatis.extension.core.parser.def.InsertSqlSourceBuilder
     */
    int insert(T entity);

    /**
     * @see com.github.developframework.mybatis.extension.core.parser.def.InsertAllSqlSourceBuilder
     */
    int insertAll(Collection<T> entity);

    /**
     * @see com.github.developframework.mybatis.extension.core.parser.def.ReplaceSqlSourceBuilder
     */
    int replace(T entity);

    /**
     * @see com.github.developframework.mybatis.extension.core.parser.def.ReplaceAllSqlSourceBuilder
     */
    int replaceAll(Collection<T> entity);

    /**
     * @see com.github.developframework.mybatis.extension.core.parser.def.UpdateSqlSourceBuilder
     */
    int update(T entity);

    /**
     * @see com.github.developframework.mybatis.extension.core.parser.def.DeleteByIdSqlSourceBuilder
     */
    int deleteById(ID id);

    /**
     * @see com.github.developframework.mybatis.extension.core.parser.def.ExistsByIdSqlSourceBuilder
     */
    boolean existsById(ID id);

    /**
     * @see com.github.developframework.mybatis.extension.core.parser.def.SelectByIdSqlSourceBuilder
     */
    Optional<T> selectById(ID id);

    /**
     * @see com.github.developframework.mybatis.extension.core.parser.def.SelectByIdLockSqlSourceBuilder
     */
    Optional<T> selectByIdLock(@Param("id") ID id, @Param(ParameterKeys.LOCK) LockType lockType);

    /**
     * @see com.github.developframework.mybatis.extension.core.parser.def.SelectByIdArraySqlSourceBuilder
     */
    List<T> selectByIdArray(ID[] ids);

    /**
     * @see com.github.developframework.mybatis.extension.core.parser.def.SelectByIdArrayLockSqlSourceBuilder
     */
    List<T> selectByIdArrayLock(@Param("ids") ID[] ids, @Param(ParameterKeys.LOCK) LockType lockType);

    /**
     * @see com.github.developframework.mybatis.extension.core.parser.def.SelectByIdsSqlSourceBuilder
     */
    List<T> selectByIds(Collection<ID> ids);

    /**
     * @see com.github.developframework.mybatis.extension.core.parser.def.SelectByIdsLockSqlSourceBuilder
     */
    List<T> selectByIdsLock(@Param("ids") Collection<ID> ids, @Param(ParameterKeys.LOCK) LockType lockType);

    /**
     * @see com.github.developframework.mybatis.extension.core.parser.def.SelectAllSqlSourceBuilder
     */
    List<T> selectAll();

    /**
     * @see com.github.developframework.mybatis.extension.core.parser.def.DescSqlSourceBuilder
     */
    List<ColumnDesc> desc();

    /**
     * @see com.github.developframework.mybatis.extension.core.parser.def.CreateTableSqlSourceBuilder
     */
    void createTable();

    /**
     * @see com.github.developframework.mybatis.extension.core.parser.def.ShowIndexSqlSourceBuilder
     */
    List<IndexDesc> showIndex();

    /**
     * @see com.github.developframework.mybatis.extension.core.parser.def.AlterSqlSourceBuilder
     */
    void alter(List<String> alterColumns);

    /**
     * 拦截器自动拼装简单查询存在SQL
     *
     * @see com.github.developframework.mybatis.extension.core.interceptors.inner.SqlCriteriaAssemblerInnerInterceptor
     */
    @Select(AUTOMATIC_SQL)
    boolean exists(SqlCriteriaAssembler assembler);

    /**
     * 拦截器自动拼装简单查询SQL
     *
     * @see com.github.developframework.mybatis.extension.core.interceptors.inner.SqlCriteriaAssemblerInnerInterceptor
     */
    @Select(AUTOMATIC_SQL)
    List<T> select(SqlCriteriaAssembler assembler, SqlSortPart sort);

    /**
     * 拦截器自动拼装简单分页查询SQL
     *
     * @see com.github.developframework.mybatis.extension.core.interceptors.inner.PagingInnerInterceptor
     */
    @Select(AUTOMATIC_SQL)
    Page<T> selectPager(Pager pager, SqlCriteriaAssembler assembler, SqlSortPart sort);

}
