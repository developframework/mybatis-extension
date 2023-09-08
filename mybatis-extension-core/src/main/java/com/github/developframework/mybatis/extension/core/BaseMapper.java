package com.github.developframework.mybatis.extension.core;

import com.github.developframework.mybatis.extension.core.structs.ColumnDesc;
import com.github.developframework.mybatis.extension.core.structs.IndexDesc;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author qiushui on 2023-08-30.
 * @see com.github.developframework.mybatis.extension.core.parser.def.BaseMapperDefaultParser
 */
public interface BaseMapper<T, ID extends Serializable> {

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
     * @see com.github.developframework.mybatis.extension.core.parser.def.SelectByIdArraySqlSourceBuilder
     */
    List<T> selectByIdArray(ID[] ids);

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

}
