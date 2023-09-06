package com.github.developframework.mybatis.extension.core.parser.def;

import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MapperMethodParseWrapper;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;

/**
 * @author qiushui on 2023-08-30.
 */
public interface SqlSourceBuilder {

    String methedName();

    MapperMethodParseWrapper build(Configuration configuration, EntityDefinition entityDefinition, Method method);
}
