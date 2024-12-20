package com.github.developframework.mybatis.extension.core.dialect;

import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;

/**
 * @author qiushui on 2024-12-20.
 */
public interface SqlSourceBuilder {

    /**
     * 创建表语句
     */
    SqlSource buildCreateTableSql(Configuration configuration, EntityDefinition entityDefinition);

    /**
     * 修改表语句
     */
    SqlSource buildAlterTableSql(Configuration configuration, EntityDefinition entityDefinition);

    /**
     * 删除语句
     */
    SqlSource buildDeleteByIdSql(Configuration configuration, EntityDefinition entityDefinition);

    /**
     * 查询字段语句
     */
    SqlSource buildDescSql(Configuration configuration, EntityDefinition entityDefinition);

    /**
     * 查询存在语句
     */
    SqlSource buildExistsByIdSql(Configuration configuration, EntityDefinition entityDefinition);

    /**
     * 批量插入语句
     */
    SqlSource buildInsertAllSql(Configuration configuration, EntityDefinition entityDefinition, Method method);

    /**
     * 插入语句
     */
    SqlSource buildInsertSql(Configuration configuration, EntityDefinition entityDefinition, Method method);

    /**
     * 批量替换语句
     */
    SqlSource buildReplaceAllSql(Configuration configuration, EntityDefinition entityDefinition, Method method);

    /**
     * 替换语句
     */
    SqlSource buildReplaceSql(Configuration configuration, EntityDefinition entityDefinition, Method method);

    /**
     * 查询全部语句
     */
    SqlSource buildSelectAllSql(Configuration configuration, EntityDefinition entityDefinition, Method method);

    /**
     * 根据ID数组查询语句
     */
    SqlSource buildSelectByIdArraySql(Configuration configuration, EntityDefinition entityDefinition, Method method, boolean lock);

    /**
     * 根据ID查询语句
     */
    SqlSource buildSelectByIdSql(Configuration configuration, EntityDefinition entityDefinition, Method method, boolean lock);

    /**
     * 显示索引语句
     */
    SqlSource buildShowIndexSql(Configuration configuration, EntityDefinition entityDefinition, Method method);

    /**
     * 修改语句
     */
    SqlSource buildUpdateSql(Configuration configuration, EntityDefinition entityDefinition, Method method);
}
