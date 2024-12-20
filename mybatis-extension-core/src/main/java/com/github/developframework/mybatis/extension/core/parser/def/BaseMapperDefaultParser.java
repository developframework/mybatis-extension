package com.github.developframework.mybatis.extension.core.parser.def;

import com.github.developframework.mybatis.extension.core.dialect.SqlSourceBuilder;
import com.github.developframework.mybatis.extension.core.parser.MapperMethodParser;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MapperMethodParseWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;

/**
 * @author qiushui on 2023-09-04.
 */
@RequiredArgsConstructor
public class BaseMapperDefaultParser implements MapperMethodParser {

    private final Configuration configuration;

    private static final MapperMethodParseHandler[] DEFAULT_HANDLERS = {
            new InsertSqlParseHandler(),
            new InsertAllSqlParseHandler(),
            new ReplaceSqlParseHandler(),
            new ReplaceAllSqlParseHandler(),
            new UpdateSqlParseHandler(),
            new DeleteByIdSqlParseHandler(),

            new ExistsByIdSqlParseHandler(),
            new SelectByIdSqlParseHandler(),
            new SelectByIdLockSqlParseHandler(),
            new SelectByIdArraySqlParseHandler(),
            new SelectByIdArrayLockSqlParseHandler(),
            new SelectByIdsSqlParseHandler(),
            new SelectByIdsLockSqlParseHandler(),
            new SelectAllSqlParseHandler(),

            new CreateTableSqlParseHandler(),
            new AlterSqlParseHandler(),
            new ShowIndexSqlParseHandler(),
            new DescSqlParseHandler(),
    };

    @Override
    public MapperMethodParseWrapper parse(EntityDefinition entityDefinition, Method method) {
        String methodName = method.getName();
        final SqlSourceBuilder sqlSourceBuilder = entityDefinition.getDialect().sqlFragmentBuilder();
        for (MapperMethodParseHandler handler : DEFAULT_HANDLERS) {
            if (handler.methodName().equals(methodName)) {
                return handler.handle(configuration, entityDefinition, sqlSourceBuilder, method);
            }
        }
        return null;
    }
}
