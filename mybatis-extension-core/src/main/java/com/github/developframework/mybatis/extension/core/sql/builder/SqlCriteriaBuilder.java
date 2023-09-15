package com.github.developframework.mybatis.extension.core.sql.builder;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.parser.naming.Operate;
import com.github.developframework.mybatis.extension.core.sql.MixedSqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.SqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.SqlFieldPart;
import com.github.developframework.mybatis.extension.core.utils.NameUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.ibatis.scripting.xmltags.IfSqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;

import java.lang.reflect.Field;

/**
 * @author qiushui on 2023-09-15.
 */
@RequiredArgsConstructor
public class SqlCriteriaBuilder {

    private final Class<?> targetClass;

    @SneakyThrows(NoSuchFieldException.class)
    private Field getField(String valueProperty) {
        return targetClass.getDeclaredField(valueProperty);
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

    public SqlCriteria eq(SqlFieldPart fieldPart, String valueProperty) {
        return () ->
                interval -> {
                    final String content = Operate.EQ.getFormat().formatted(fieldPart.toSql(), NameUtils.placeholder(valueProperty));
                    return new IfSqlNode(new StaticTextSqlNode(content), valueProperty + "neq null");
                };
    }
}
