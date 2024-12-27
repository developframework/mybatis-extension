package com.github.developframework.mybatis.extension.core.sql.criteria;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.parser.naming.Operate;
import com.github.developframework.mybatis.extension.core.sql.FieldSqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.SqlFieldPart;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaBuilderContext;
import com.github.developframework.mybatis.extension.core.utils.NameUtils;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.scripting.xmltags.ChooseSqlNode;
import org.apache.ibatis.scripting.xmltags.IfSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author qiushui on 2024-12-27.
 */
@RequiredArgsConstructor
public class BetweenSqlCriteria extends FieldSqlCriteria {

    private final SqlFieldPart fieldPart;

    private final Object value1;

    private final Object value2;

    @Override
    public Function<Interval, SqlNode> toSqlNode(Configuration configuration, SqlCriteriaBuilderContext context) {
        return interval -> {
            final String paramName1, finalValue1, paramName2, finalValue2;
            if (value1 == null) {
                paramName1 = null;
                finalValue1 = null;
            } else if (value1 instanceof SqlFieldPart sfp) {
                paramName1 = null;
                finalValue1 = sfp.toSql();
            } else {
                paramName1 = context.collectParam(value1);
                finalValue1 = NameUtils.placeholder(paramName1);
            }
            if (value2 == null) {
                paramName2 = null;
                finalValue2 = null;
            } else if (value2 instanceof SqlFieldPart sfp) {
                paramName2 = null;
                finalValue2 = sfp.toSql();
            } else {
                paramName2 = context.collectParam(value2);
                finalValue2 = NameUtils.placeholder(paramName2);
            }
            List<SqlNode> ifSqlNodes = new ArrayList<>(3);
            ifSqlNodes.add(
                    new IfSqlNode(
                            new StaticTextSqlNode(
                                    interval.getText() + Operate.BETWEEN.getFormat().formatted(fieldPart.toSql(), finalValue1, finalValue2)
                            ),
                            paramName1 + " neq null and " + paramName2 + " neq null"
                    )
            );
            ifSqlNodes.add(
                    new IfSqlNode(
                            new StaticTextSqlNode(
                                    interval.getText() + Operate.GTE.getFormat().formatted(fieldPart.toSql(), finalValue1)
                            ),
                            paramName1 + " neq null"
                    )
            );
            ifSqlNodes.add(
                    new IfSqlNode(
                            new StaticTextSqlNode(
                                    interval.getText() + Operate.LTE.getFormat().formatted(fieldPart.toSql(), finalValue2)
                            ),
                            paramName2 + " neq null"
                    )
            );
            return new ChooseSqlNode(ifSqlNodes, null);
        };
    }
}
