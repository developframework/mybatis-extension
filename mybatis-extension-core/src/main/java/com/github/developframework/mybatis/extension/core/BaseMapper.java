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

    int insert(T entity);

    int insertAll(Collection<T> entity);

    int replace(T entity);

    int replaceAll(Collection<T> entity);

    Optional<T> selectById(ID id);

    List<ColumnDesc> desc();

    void createTable();

    List<IndexDesc> showIndex();

    //
    void alter(List<String> alterColumns);

}
