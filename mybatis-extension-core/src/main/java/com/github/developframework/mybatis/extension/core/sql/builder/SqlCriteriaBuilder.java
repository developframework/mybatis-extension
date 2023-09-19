package com.github.developframework.mybatis.extension.core.sql.builder;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.parser.naming.Operate;
import com.github.developframework.mybatis.extension.core.sql.MixedSqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.SqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.SqlFieldPart;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.ParameterKeys;
import com.github.developframework.mybatis.extension.core.utils.NameUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.scripting.xmltags.IfSqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;

import java.lang.reflect.Field;

/**
 * @author qiushui on 2023-09-15.
 */
@RequiredArgsConstructor
public class SqlCriteriaBuilder {

    private final EntityDefinition entityDefinition;

    @Getter
    private final MapperMethod.ParamMap<Object> criteriaParamMap = new MapperMethod.ParamMap<>();

    private int criteriaParamIndex;

    @SneakyThrows(NoSuchFieldException.class)
    private Field getField(String valueProperty) {
        return entityDefinition.getEntityClass().getDeclaredField(valueProperty);
    }

    public SqlCriteria and(SqlCriteria... criteriaChain) {
        if (criteriaChain.length == 1) {
            return criteriaChain[0];
        } else {
            return new MixedSqlCriteria(Interval.AND, criteriaChain);
        }
    }

    public SqlCriteria or(SqlCriteria... criteriaChain) {
        if (criteriaChain.length == 1) {
            return criteriaChain[0];
        } else {
            return new MixedSqlCriteria(Interval.OR, criteriaChain);
        }
    }

    public SqlCriteria eq(SqlFieldPart fieldPart, Object value) {
        final String paramName = collectParam(value);
        return () ->
                interval -> {
                    final String content = interval.getText() + Operate.EQ.getFormat().formatted(fieldPart.toSql(), NameUtils.placeholder(paramName));
                    return new IfSqlNode(new StaticTextSqlNode(content), paramName + " neq null");
                };
    }

    private String collectParam(Object value) {
        String paramName = ParameterKeys.CRITERIA_PARAM + criteriaParamIndex++;
        criteriaParamMap.put(paramName, value);
        return paramName;
    }
}
