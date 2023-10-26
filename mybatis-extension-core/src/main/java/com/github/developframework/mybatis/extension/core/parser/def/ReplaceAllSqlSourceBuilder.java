package com.github.developframework.mybatis.extension.core.parser.def;

import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MapperMethodParseWrapper;
import com.github.developframework.mybatis.extension.core.utils.MybatisUtils;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author qiushui on 2023-09-06.
 */
@RequiredArgsConstructor
public class ReplaceAllSqlSourceBuilder implements SqlSourceBuilder {

    @Override
    public String methedName() {
        return "replaceAll";
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
                values.append(columnDefinition.getColumnMybatisPlaceholder().placeholder("it." + columnDefinition.getProperty()));
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
        sb.append("INTO ").append(entityDefinition.wrapTableName());
        sb.append(fields);
        sb.append(" VALUES");

        final ForEachSqlNode forEachSqlNode = new ForEachSqlNode(
                configuration,
                new StaticTextSqlNode(values.toString()),
                MybatisUtils.getCollectionExpression(method, null),
                false,
                null,
                "it",
                null,
                null,
                ","
        );
        final MixedSqlNode mixedSqlNode = new MixedSqlNode(List.of(new StaticTextSqlNode(sb.toString()), forEachSqlNode));
        SqlSource sqlSource = new DynamicSqlSource(configuration, mixedSqlNode);
        return new MapperMethodParseWrapper(SqlCommandType.INSERT, sqlSource);
    }
}
