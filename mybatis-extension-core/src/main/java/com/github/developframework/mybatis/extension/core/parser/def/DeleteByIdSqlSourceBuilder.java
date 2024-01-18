package com.github.developframework.mybatis.extension.core.parser.def;

import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MapperMethodParseWrapper;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.LinkedList;
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
        final SqlSource sqlSource;
        if (entityDefinition.hasLogicDelete()) {
            sqlSource = buildUpdateSqlSource(configuration, entityDefinition);
        } else {
            sqlSource = buildDeleteSqlSource(configuration, entityDefinition);
        }
        return new MapperMethodParseWrapper(SqlCommandType.UPDATE, sqlSource);
    }

    private SqlSource buildUpdateSqlSource(Configuration configuration, EntityDefinition entityDefinition) {
        final String whereSql = buildWhereByIdSql(entityDefinition);
        final StaticTextSqlNode setContentSqlNode = new StaticTextSqlNode(String.format("%s = 1", entityDefinition.getLogicDeleteColumnDefinition().wrapColumn()));
        final List<SqlNode> sqlNodes = new LinkedList<>();
        sqlNodes.add(new StaticTextSqlNode("UPDATE " + entityDefinition.wrapTableName()));
        sqlNodes.add(new SetSqlNode(configuration, setContentSqlNode));
        sqlNodes.add(new StaticTextSqlNode(whereSql));
        if (entityDefinition.hasMultipleTenant()) {
            sqlNodes.addAll(multipleTenantSqlNodes(entityDefinition));
        }
        return new DynamicSqlSource(configuration, new MixedSqlNode(sqlNodes));
    }

    private SqlSource buildDeleteSqlSource(Configuration configuration, EntityDefinition entityDefinition) {
        final String sql = "DELETE FROM " + entityDefinition.wrapTableName() + buildWhereByIdSql(entityDefinition);
        if (entityDefinition.hasMultipleTenant()) {
            final List<SqlNode> sqlNodes = new LinkedList<>();
            sqlNodes.add(new StaticTextSqlNode(sql));
            sqlNodes.addAll(multipleTenantSqlNodes(entityDefinition));
            final MixedSqlNode mixedSqlNode = new MixedSqlNode(sqlNodes);
            return new DynamicSqlSource(configuration, mixedSqlNode);
        } else {
            return new RawSqlSource(configuration, sql, entityDefinition.getEntityClass());
        }
    }
}
