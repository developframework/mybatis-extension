package com.github.developframework.mybatis.extension.core.sql.criteria;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.parser.naming.Operate;
import com.github.developframework.mybatis.extension.core.sql.FieldSqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.SqlFieldPart;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaBuilderContext;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;

/**
 * @author qiushui on 2024-12-27.
 */
@RequiredArgsConstructor
public class LiteralSqlCriteria extends FieldSqlCriteria {

    private final SqlFieldPart fieldPart;

    private final Operate operate;

    @Override
    public SqlNode toSqlNode(Configuration configuration, SqlCriteriaBuilderContext context, Interval interval) {
        return new StaticTextSqlNode(
                interval.getText() + operate.getFormat().formatted(fieldPart.toSql())
        );
    }
}
