package com.github.developframework.mybatis.extension.core.parser;

import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MapperMethodParseWrapper;

import java.lang.reflect.Method;

/**
 * @author qiushui on 2023-08-30.
 */
public interface MapperMethodParser {

    /**
     * 解析成SqlCommandType和SqlSource
     *
     * @param entityDefinition 实体定义
     * @param method           Mapper方法
     * @return SqlSource
     */
    MapperMethodParseWrapper parse(EntityDefinition entityDefinition, Method method);
}
