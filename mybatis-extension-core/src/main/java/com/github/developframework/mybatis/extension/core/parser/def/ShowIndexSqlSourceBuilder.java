package com.github.developframework.mybatis.extension.core.parser.def;

import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MapperMethodParseWrapper;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;

/**
 * @author qiushui on 2023-09-06.
 */
public class ShowIndexSqlSourceBuilder implements SqlSourceBuilder {

    @Override
    public String methedName() {
        return "showIndex";
    }

    @Override
    public MapperMethodParseWrapper build(Configuration configuration, EntityDefinition entityDefinition, Method method) {
        final String sql = "SHOW INDEX FROM " + entityDefinition.wrapTableName() + " WHERE Key_name != 'PRIMARY'";
        SqlSource sqlSource = new RawSqlSource(configuration, sql, null);
        return new MapperMethodParseWrapper(SqlCommandType.SELECT, sqlSource);
    }
}
