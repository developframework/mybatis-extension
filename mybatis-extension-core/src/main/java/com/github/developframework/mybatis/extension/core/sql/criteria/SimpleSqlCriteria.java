package com.github.developframework.mybatis.extension.core.sql.criteria;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.parser.naming.Operate;
import com.github.developframework.mybatis.extension.core.sql.FieldSqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaBuilderContext;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;

/**
 * @author qiushui on 2024-12-27.
 */
@RequiredArgsConstructor
public class SimpleSqlCriteria extends FieldSqlCriteria {

    private final Object value1;

    private final Object value2;

    private final Operate operate;

    @Override
    public SqlNode toSqlNode(Configuration configuration, SqlCriteriaBuilderContext context, Interval interval) {
        final CriteriaParameter parameter1 = context.newParameter(value1);
        final CriteriaParameter parameter2 = context.newParameter(value2);
        return buildSqlNodeCheckNull(
                new StaticTextSqlNode(
                        interval.getText() + operate.getFormat().formatted(parameter1.finalValue(), parameter2.finalValue())
                ),
                parameter1,
                parameter2
        );
    }
}
