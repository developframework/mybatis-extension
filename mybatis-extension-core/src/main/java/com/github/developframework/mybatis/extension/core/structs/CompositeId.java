package com.github.developframework.mybatis.extension.core.structs;

import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qiushui on 2023-09-19.
 */
public class CompositeId implements Serializable {

    @Getter
    private final Map<String, Object> innerMap = new HashMap<>();

    public CompositeId id(String property, Object value) {
        innerMap.put(property, value);
        return this;
    }

    public CompositeId ids(Map<String, Object> idMap) {
        innerMap.putAll(idMap);
        return this;
    }
}
