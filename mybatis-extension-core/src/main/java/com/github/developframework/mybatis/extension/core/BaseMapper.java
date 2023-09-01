package com.github.developframework.mybatis.extension.core;

import com.github.developframework.mybatis.extension.core.structs.ColumnDesc;
import com.github.developframework.mybatis.extension.core.structs.IndexDesc;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * @author qiushui on 2023-08-30.
 */
public interface BaseMapper<T, PK extends Serializable> {

    Optional<T> selectByPK(PK primaryKey);

    List<ColumnDesc> desc();

    void createTable();

    List<IndexDesc> showIndex();

    //
    void alter(List<String> alterColumns);

}
