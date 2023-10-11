package com.github.developframework.mybatis.extension.core.idgenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author qiushui on 2023-10-11.
 */
public class IdGeneratorRegistry {

    private final Map<Class<? extends IdGenerator>, IdGenerator> internalMap = new HashMap<>();

    public void register(IdGenerator idGenerator) {
        internalMap.putIfAbsent(idGenerator.getClass(), idGenerator);
    }

    public IdGenerator get(Class<?> idGeneratorClass) {
        final IdGenerator idGenerator = internalMap.get(idGeneratorClass);
        if (idGenerator == null) {
            throw new IllegalArgumentException("不存在" + idGeneratorClass.getName());
        }
        return idGenerator;
    }
}
