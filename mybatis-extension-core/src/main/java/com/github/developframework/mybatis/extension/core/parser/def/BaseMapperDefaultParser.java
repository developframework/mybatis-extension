package com.github.developframework.mybatis.extension.core.parser.def;

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

    private static final SqlSourceBuilder[] DEFAULT_BUILDER = {
            new InsertSqlSourceBuilder(),
            new InsertAllSqlSourceBuilder(),
            new ReplaceSqlSourceBuilder(),
            new ReplaceAllSqlSourceBuilder(),
            new UpdateSqlSourceBuilder(),
            new DeleteByIdSqlSourceBuilder(),

            new ExistsByIdSqlSourceBuilder(),
            new SelectByIdSqlSourceBuilder(),
            new SelectByIdLockSqlSourceBuilder(),
            new SelectByIdArraySqlSourceBuilder(),
            new SelectByIdArrayLockSqlSourceBuilder(),
            new SelectByIdsSqlSourceBuilder(),
            new SelectByIdsLockSqlSourceBuilder(),
            new SelectAllSqlSourceBuilder(),

            new CreateTableSqlSourceBuilder(),
            new AlterSqlSourceBuilder(),
            new ShowIndexSqlSourceBuilder(),
            new DescSqlSourceBuilder(),
    };

    @Override
    public MapperMethodParseWrapper parse(EntityDefinition entityDefinition, Method method) {
        String methodName = method.getName();
        for (SqlSourceBuilder sqlSourceBuilder : DEFAULT_BUILDER) {
            if (sqlSourceBuilder.methedName().equals(methodName)) {
                return sqlSourceBuilder.build(configuration, entityDefinition, method);
            }
        }
        return null;
    }
}
