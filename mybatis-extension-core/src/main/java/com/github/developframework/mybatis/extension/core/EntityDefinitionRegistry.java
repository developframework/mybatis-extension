package com.github.developframework.mybatis.extension.core;

import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * @author qiushui on 2023-08-30.
 */
public class EntityDefinitionRegistry {

    private final Map<Class<?>, EntityDefinition> internalMap = new HashMap<>();

    public EntityDefinition register(Class<?> entityClass) {
        return internalMap.computeIfAbsent(entityClass, EntityDefinition::new);
    }

    public EntityDefinition get(Class<?> entityClass) {
        final EntityDefinition entityDefinition = internalMap.get(entityClass);
        if (entityDefinition == null) {
            throw new IllegalArgumentException("不存在" + entityClass.getName() + "的定义");
        }
        return entityDefinition;
    }
}