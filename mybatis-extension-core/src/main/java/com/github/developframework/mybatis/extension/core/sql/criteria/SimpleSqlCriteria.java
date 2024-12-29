package com.github.developframework.mybatis.extension.core.sql.criteria;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.parser.naming.Operate;
import com.github.developframework.mybatis.extension.core.sql.FieldSqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.SqlFieldPart;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaBuilderContext;
import com.github.developframework.mybatis.extension.core.utils.NameUtils;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;

/**
 * @author qiushui on 2024-12-27.
 */
@RequiredArgsConstructor
public class SimpleSqlCriteria extends FieldSqlCriteria {

    private final SqlFieldPart fieldPart;

    private final Object value;

    private final Operate operate;

    @Override
    public SqlNode toSqlNode(Configuration configuration, SqlCriteriaBuilderContext context, Interval interval) {
        final String paramName, finalValue;
        if (value instanceof SqlFieldPart sfp) {
            paramName = null;
            finalValue = sfp.toSql();
        } else {
            paramName = context.collectParam(value);
            finalValue = NameUtils.placeholder(paramName);
        }
        StaticTextSqlNode staticTextSqlNode = new StaticTextSqlNode(
                interval.getText() + operate.getFormat().formatted(fieldPart.toSql(), finalValue)
        );
        return buildIfSqlNode(paramName, fieldPart, staticTextSqlNode);
    }
}
