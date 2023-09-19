package com.github.developframework.mybatis.extension.core.sql;

import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;

/**
 * @author qiushui on 2023-09-19.
 */
public interface SqlSortPart extends SqlPart {

    String toSql(EntityDefinition entityDefinition);
}
