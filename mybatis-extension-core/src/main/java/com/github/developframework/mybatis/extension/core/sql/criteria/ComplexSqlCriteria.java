package com.github.developframework.mybatis.extension.core.sql.criteria;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.sql.FieldSqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaBuilderContext;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.session.Configuration;

import java.util.function.Function;

/**
 * @author qiushui on 2024-12-27.
 */
@RequiredArgsConstructor
public class ComplexSqlCriteria extends FieldSqlCriteria {

    private final Function<Interval, SqlNode> function;

    @Override
    public SqlNode toSqlNode(Configuration configuration, SqlCriteriaBuilderContext context, Interval interval) {
        return function.apply(interval);
    }
}
