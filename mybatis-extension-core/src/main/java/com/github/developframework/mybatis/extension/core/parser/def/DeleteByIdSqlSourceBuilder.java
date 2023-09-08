package com.github.developframework.mybatis.extension.core.parser.def;

import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MapperMethodParseWrapper;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author qiushui on 2023-09-08.
 */
public class DeleteByIdSqlSourceBuilder extends AbstractSqlSourceBuilder {
    @Override
    public String methedName() {
        return "deleteById";
    }

    @Override
    public MapperMethodParseWrapper build(Configuration configuration, EntityDefinition entityDefinition, Method method) {
        final String sql = buildSql(entityDefinition, "DELETE");
        SqlSource sqlSource;
        if (entityDefinition.hasMultipleTenant()) {
            final MixedSqlNode mixedSqlNode = new MixedSqlNode(
                    List.of(
                            new StaticTextSqlNode(sql),
                            multipleTenantSqlNodes(entityDefinition)
                    )
            );
            sqlSource = new DynamicSqlSource(configuration, mixedSqlNode);
        } else {
            sqlSource = new RawSqlSource(configuration, sql, entityDefinition.getEntityClass());
        }
        return new MapperMethodParseWrapper(SqlCommandType.UPDATE, sqlSource);
    }
}
