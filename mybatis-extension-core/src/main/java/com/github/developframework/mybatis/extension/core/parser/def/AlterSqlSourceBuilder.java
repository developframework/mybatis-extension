package com.github.developframework.mybatis.extension.core.parser.def;

import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MapperMethodParseWrapper;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author qiushui on 2023-09-06.
 */
public class AlterSqlSourceBuilder implements SqlSourceBuilder {

    @Override
    public String methedName() {
        return "alter";
    }

    @Override
    public MapperMethodParseWrapper build(Configuration configuration, EntityDefinition entityDefinition, Method method) {
        StaticTextSqlNode staticTextSqlNode = new StaticTextSqlNode("ALTER TABLE " + entityDefinition.wrapTableName() + " ");
        final ForEachSqlNode forEachSqlNode = new ForEachSqlNode(
                configuration,
                new TextSqlNode("${it}"),
                "collection",
                false,
                null,
                "it",
                null,
                null,
                ","
        );
        SqlSource sqlSource = new DynamicSqlSource(configuration, new MixedSqlNode(List.of(staticTextSqlNode, forEachSqlNode)));
        return new MapperMethodParseWrapper(SqlCommandType.UPDATE, sqlSource);
    }
}
