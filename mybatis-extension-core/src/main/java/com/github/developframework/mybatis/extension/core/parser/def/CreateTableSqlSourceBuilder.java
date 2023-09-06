package com.github.developframework.mybatis.extension.core.parser.def;

import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import com.github.developframework.mybatis.extension.core.structs.ColumnDesc;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MapperMethodParseWrapper;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author qiushui on 2023-09-06.
 */
public class CreateTableSqlSourceBuilder implements SqlSourceBuilder {

    @Override
    public String methedName() {
        return "createTable";
    }

    @Override
    public MapperMethodParseWrapper build(Configuration configuration, EntityDefinition entityDefinition, Method method) {
        final String sql =
                new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                        .append(entityDefinition.wrapTableName())
                        .append(" (")
                        .append(
                                entityDefinition.getColumnDefinitions()
                                        .values()
                                        .stream()
                                        .map(cd -> ColumnDesc.fromColumnDefinition(cd).toString())
                                        .collect(Collectors.joining(","))
                        )
                        .append(", PRIMARY KEY (")
                        .append(
                                Arrays.stream(entityDefinition.getPrimaryKeyColumnDefinitions())
                                        .map(ColumnDefinition::getColumn)
                                        .collect(Collectors.joining(","))
                        )
                        .append(")")
                        .append(") ENGINE=").append(entityDefinition.getEngine().name())
                        .append(entityDefinition.getComment().isEmpty() ? "" : (String.format(" COMMENT = '%s'", entityDefinition.getComment())))
                        .toString();
        SqlSource sqlSource = new RawSqlSource(configuration, sql, null);
        return new MapperMethodParseWrapper(SqlCommandType.UPDATE, sqlSource);
    }
}
