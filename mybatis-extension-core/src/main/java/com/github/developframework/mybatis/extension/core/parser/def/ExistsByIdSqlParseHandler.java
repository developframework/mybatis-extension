package com.github.developframework.mybatis.extension.core.parser.def;

import com.github.developframework.mybatis.extension.core.dialect.SqlSourceBuilder;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MapperMethodParseWrapper;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;

/**
 * @author qiushui on 2023-09-08.
 */
public class ExistsByIdSqlParseHandler extends AbstractSqlParseHandler {
    @Override
    public String methodName() {
        return "existsById";
    }

    @Override
    public MapperMethodParseWrapper handle(Configuration configuration, EntityDefinition entityDefinition, SqlSourceBuilder sqlSourceBuilder, Method method) {
        final SqlSource sqlSource = sqlSourceBuilder.buildExistsByIdSql(configuration, entityDefinition);
        return new MapperMethodParseWrapper(SqlCommandType.SELECT, sqlSource);
    }
}
