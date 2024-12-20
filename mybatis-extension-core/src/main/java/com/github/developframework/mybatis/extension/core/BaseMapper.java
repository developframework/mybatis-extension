package com.github.developframework.mybatis.extension.core;

import com.github.developframework.mybatis.extension.core.dialect.ColumnDescription;
import com.github.developframework.mybatis.extension.core.parser.def.*;
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
     * @see InsertSqlParseHandler
     */
    int insert(T entity);

    /**
     * @see InsertAllSqlParseHandler
     */
    int insertAll(Collection<T> entity);

    /**
     * @see ReplaceSqlParseHandler
     */
    int replace(T entity);

    /**
     * @see ReplaceAllSqlParseHandler
     */
    int replaceAll(Collection<T> entity);

    /**
     * @see UpdateSqlParseHandler
     */
    int update(T entity);

    /**
     * @see DeleteByIdSqlParseHandler
     */
    int deleteById(ID id);

    /**
     * @see ExistsByIdSqlParseHandler
     */
    boolean existsById(ID id);

    /**
     * @see SelectByIdSqlParseHandler
     */
    Optional<T> selectById(ID id);

    /**
     * @see SelectByIdLockSqlParseHandler
     */
    Optional<T> selectByIdLock(@Param("id") ID id, @Param(ParameterKeys.LOCK) LockType lockType);

    /**
     * @see SelectByIdArraySqlParseHandler
     */
    List<T> selectByIdArray(ID[] ids);

    /**
     * @see SelectByIdArrayLockSqlParseHandler
     */
    List<T> selectByIdArrayLock(@Param("ids") ID[] ids, @Param(ParameterKeys.LOCK) LockType lockType);

    /**
     * @see SelectByIdsSqlParseHandler
     */
    List<T> selectByIds(Collection<ID> ids);

    /**
     * @see SelectByIdsLockSqlParseHandler
     */
    List<T> selectByIdsLock(@Param("ids") Collection<ID> ids, @Param(ParameterKeys.LOCK) LockType lockType);

    /**
     * @see SelectAllSqlParseHandler
     */
    List<T> selectAll();

    /**
     * @see DescSqlParseHandler
     */
    List<ColumnDescription> desc();

    /**
     * @see CreateTableSqlParseHandler
     */
    void createTable();

    /**
     * @see ShowIndexSqlParseHandler
     */
    List<IndexDesc> showIndex();

    /**
     * @see AlterSqlParseHandler
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
     * 拦截器自动拼装简单查询存在SQL
     *
     * @see com.github.developframework.mybatis.extension.core.interceptors.inner.SqlCriteriaAssemblerInnerInterceptor
     */
    @Select(AUTOMATIC_SQL)
    int count(SqlCriteriaAssembler assembler);

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
