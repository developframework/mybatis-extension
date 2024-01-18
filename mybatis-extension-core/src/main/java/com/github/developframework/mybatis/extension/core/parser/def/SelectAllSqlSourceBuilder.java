package com.github.developframework.mybatis.extension.core.parser.def;

import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MapperMethodParseWrapper;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * @author qiushui on 2023-09-14.
 */
public class SelectAllSqlSourceBuilder extends AbstractSqlSourceBuilder {
    @Override
    public String methedName() {
        return "selectAll";
    }

    @Override
    public MapperMethodParseWrapper build(Configuration configuration, EntityDefinition entityDefinition, Method method) {
        final List<SqlNode> sqlNodes = new LinkedList<>();
        if (entityDefinition.hasLogicDelete()) {
            sqlNodes.add(new StaticTextSqlNode(String.format("%s = 0", entityDefinition.getLogicDeleteColumnDefinition().wrapColumn())));
        }
        if (entityDefinition.hasMultipleTenant()) {
            sqlNodes.addAll(multipleTenantSqlNodes(entityDefinition));
        }
        SqlSource sqlSource = new DynamicSqlSource(configuration, new MixedSqlNode(
                List.of(
                        new StaticTextSqlNode("SELECT * FROM " + entityDefinition.wrapTableName()),
                        new WhereSqlNode(configuration, new MixedSqlNode(sqlNodes))
                )
        ));
        return new MapperMethodParseWrapper(SqlCommandType.SELECT, sqlSource);
    }
}
