package com.github.developframework.mybatis.extension.core;

import com.github.developframework.mybatis.extension.core.parser.MapperMethodParser;
import com.github.developframework.mybatis.extension.core.parser.naming.MapperMethodNamingParser;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;

/**
 * @author qiushui on 2023-08-30.
 */
public class SqlSourceBuilder {

    private final Configuration configuration;

    private final MapperMethodParser[] mapperMethodParsers;

    public SqlSourceBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.mapperMethodParsers = new MapperMethodParser[]{
                new MapperMethodNamingParser(configuration)
        };
    }

    public SqlSource build(EntityDefinition entityDefinition, Method method) {
        switch (method.getName()) {

        }


        return null;
    }
}
