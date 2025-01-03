package com.github.developframework.mybatis.extension.core.sql.criteria;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.parser.naming.Operate;
import com.github.developframework.mybatis.extension.core.sql.FieldSqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaBuilderContext;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.scripting.xmltags.ChooseSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qiushui on 2024-12-27.
 */
@RequiredArgsConstructor
public class BetweenSqlCriteria extends FieldSqlCriteria {

    private final Object target;

    private final Object value1;

    private final Object value2;

    @Override
    public SqlNode toSqlNode(Configuration configuration, SqlCriteriaBuilderContext context, Interval interval) {
        final CriteriaParameter targetParameter = context.newParameter(target);
        final CriteriaParameter parameter1 = context.newParameter(value1);
        final CriteriaParameter parameter2 = context.newParameter(value2);
        final List<SqlNode> ifSqlNodes = new ArrayList<>(3);

        ifSqlNodes.add(
                buildSqlNodeCheckNull(
                        new StaticTextSqlNode(
                                interval.getText() + Operate.BETWEEN.getFormat().formatted(targetParameter.finalValue(), parameter1.finalValue(), parameter2.finalValue())
                        ),
                        targetParameter,
                        parameter1,
                        parameter2
                )
        );
        ifSqlNodes.add(
                buildSqlNodeCheckNull(
                        new StaticTextSqlNode(
                                interval.getText() + Operate.GTE.getFormat().formatted(targetParameter.finalValue(), parameter1.finalValue())
                        ),
                        targetParameter,
                        parameter1
                )
        );
        ifSqlNodes.add(
                buildSqlNodeCheckNull(
                        new StaticTextSqlNode(
                                interval.getText() + Operate.LTE.getFormat().formatted(targetParameter.finalValue(), parameter2.finalValue())
                        ),
                        targetParameter,
                        parameter2
                )
        );
        return new ChooseSqlNode(ifSqlNodes, null);
    }
}
