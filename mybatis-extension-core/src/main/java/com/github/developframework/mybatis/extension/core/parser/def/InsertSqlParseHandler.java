package com.github.developframework.mybatis.extension.core.parser.def;

import com.github.developframework.mybatis.extension.core.dialect.SqlSourceBuilder;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MapperMethodParseWrapper;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;

/**
 * @author qiushui on 2023-09-06.
 */
public class InsertSqlParseHandler implements MapperMethodParseHandler {

    @Override
    public String methodName() {
        return "insert";
    }

    @Override
    public MapperMethodParseWrapper handle(Configuration configuration, EntityDefinition entityDefinition, SqlSourceBuilder sqlSourceBuilder, Method method) {
        SqlSource sqlSource = sqlSourceBuilder.buildInsertSql(configuration, entityDefinition, method);
        return new MapperMethodParseWrapper(SqlCommandType.INSERT, sqlSource);
    }
}
