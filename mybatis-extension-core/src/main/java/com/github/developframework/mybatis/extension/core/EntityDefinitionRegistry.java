package com.github.developframework.mybatis.extension.core;

import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;

import java.util.HashMap;

/**
 * @author qiushui on 2023-08-30.
 */
public class EntityDefinitionRegistry extends HashMap<Class<?>, EntityDefinition> {

    @Override
    public EntityDefinition get(Object key) {
        final EntityDefinition entityDefinition = super.get(key);
        if (entityDefinition == null) {
            throw new IllegalArgumentException("不存在" + key.toString() + "的定义");
        }
        return entityDefinition;
    }
}