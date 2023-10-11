package com.github.developframework.mybatis.extension.core.autoinject;

import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import org.apache.ibatis.mapping.SqlCommandType;

/**
 * @author qiushui on 2023-09-05.
 */
public interface AutoInjectProvider {

    /**
     * 哪些SQL操作类型需要注入
     * <p>
     * INSERT or UPDATE
     */
    SqlCommandType[] needInject();

    /**
     * 提供注入值
     */
    Object provide(EntityDefinition entityDefinition, ColumnDefinition columnDefinition, Object entity);
}
