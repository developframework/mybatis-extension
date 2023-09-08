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
public class ExistsByIdSqlSourceBuilder extends AbstractSqlSourceBuilder {
    @Override
    public String methedName() {
        return "existsById";
    }

    @Override
    public MapperMethodParseWrapper build(Configuration configuration, EntityDefinition entityDefinition, Method method) {
        String sql = buildSql(entityDefinition, "SELECT 1");
        SqlSource sqlSource;
        if (entityDefinition.hasMultipleTenant()) {
            final MixedSqlNode mixedSqlNode = new MixedSqlNode(
                    List.of(
                            new StaticTextSqlNode("SELECT IFNULL(("),
                            new StaticTextSqlNode(sql),
                            multipleTenantSqlNodes(entityDefinition),
                            new StaticTextSqlNode("), 0) `exists`")
                    )
            );
            sqlSource = new DynamicSqlSource(configuration, mixedSqlNode);
        } else {
            sql = String.format("SELECT IFNULL((%s), 0) `exists`", sql);
            sqlSource = new RawSqlSource(configuration, sql, entityDefinition.getEntityClass());
        }
        return new MapperMethodParseWrapper(SqlCommandType.SELECT, sqlSource);
    }
}
