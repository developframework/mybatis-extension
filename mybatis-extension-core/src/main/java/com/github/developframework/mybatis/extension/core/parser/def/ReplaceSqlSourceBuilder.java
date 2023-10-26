package com.github.developframework.mybatis.extension.core.parser.def;

import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MapperMethodParseWrapper;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author qiushui on 2023-09-06.
 */
public class ReplaceSqlSourceBuilder implements SqlSourceBuilder {

    @Override
    public String methedName() {
        return "replace";
    }

    @Override
    public MapperMethodParseWrapper build(Configuration configuration, EntityDefinition entityDefinition, Method method) {
        StringBuilder fields = new StringBuilder(" (");
        StringBuilder values = new StringBuilder(" (");
        List<ColumnDefinition> columnDefinitions = new ArrayList<>(entityDefinition.getColumnDefinitions().values());

        for (int i = 0; i < columnDefinitions.size(); i++) {
            final ColumnDefinition columnDefinition = columnDefinitions.get(i);
            if (!columnDefinition.isAutoIncrement()) {
                fields.append(columnDefinition.wrapColumn());
                values.append(columnDefinition.placeholder());
                if (i < columnDefinitions.size() - 1) {
                    fields.append(",");
                    values.append(",");
                } else {
                    fields.append(")");
                    values.append(")");
                }
            }
        }
        StringBuilder sb = new StringBuilder("REPLACE ");
//        if (entityDefinition.isInsertIgnore()) {
//            sb.append("IGNORE ");
//        }
        sb.append("INTO ")
                .append(entityDefinition.wrapTableName())
                .append(fields)
                .append(" VALUES")
                .append(values);
        SqlSource sqlSource = new RawSqlSource(configuration, sb.toString(), entityDefinition.getEntityClass());
        return new MapperMethodParseWrapper(SqlCommandType.INSERT, sqlSource);
    }
}
