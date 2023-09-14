package com.github.developframework.mybatis.extension.core.parser.def;

import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MapperMethodParseWrapper;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * @author qiushui on 2023-09-14.
 */
public class SelectByIdLockSqlSourceBuilder extends AbstractSqlSourceBuilder {

    @Override
    public String methedName() {
        return "selectByIdLock";
    }

    @Override
    public MapperMethodParseWrapper build(Configuration configuration, EntityDefinition entityDefinition, Method method) {
        final String sql = buildSql(entityDefinition, "SELECT *");
        final List<SqlNode> sqlNodes = new LinkedList<>();
        sqlNodes.add(new StaticTextSqlNode(sql));
        if (entityDefinition.hasMultipleTenant()) {
            sqlNodes.add(multipleTenantSqlNodes(entityDefinition));
        }
        sqlNodes.add(new StaticTextSqlNode(" LIMIT 1"));
        sqlNodes.add(lockChooseSqlNode());
        SqlSource sqlSource = new DynamicSqlSource(configuration, new MixedSqlNode(sqlNodes));
        return new MapperMethodParseWrapper(SqlCommandType.SELECT, sqlSource);
    }
}
