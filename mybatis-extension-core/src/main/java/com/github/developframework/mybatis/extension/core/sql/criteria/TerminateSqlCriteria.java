package com.github.developframework.mybatis.extension.core.sql.criteria;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.sql.FieldSqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaBuilderContext;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;

import java.util.function.Function;

/**
 * @author qiushui on 2024-12-27.
 */
public class TerminateSqlCriteria extends FieldSqlCriteria {

    @Override
    public Function<Interval, SqlNode> toSqlNode(Configuration configuration, SqlCriteriaBuilderContext context) {
        return interval -> new StaticTextSqlNode(interval.getText() + "0 = 1");
    }
}
