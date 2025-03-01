package com.github.developframework.mybatis.extension.core.sql.criteria;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.parser.naming.Operate;
import com.github.developframework.mybatis.extension.core.sql.FieldSqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.SqlFieldPart;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaBuilderContext;
import com.github.developframework.mybatis.extension.core.utils.NameUtils;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;

import java.util.List;

/**
 * @author qiushui on 2024-12-27.
 */
@RequiredArgsConstructor
public class WithInSqlCriteria extends FieldSqlCriteria {

    private final SqlFieldPart fieldPart;

    private final Object value;

    private final Operate operate;

    @Override
    public SqlNode toSqlNode(Configuration configuration, SqlCriteriaBuilderContext context, Interval interval) {
        final CriteriaParameter fieldPartParameter = context.newParameter(fieldPart);
        final CriteriaParameter valueParameter = context.newParameter(value);

        final String itemName = valueParameter.paramName() + "_item";
        final ForEachSqlNode forEachSqlNode = new ForEachSqlNode(
                configuration,
                new StaticTextSqlNode(NameUtils.placeholder(itemName)),
                valueParameter.paramName(),
                true,
                null,
                itemName,
                "(",
                ")",
                ","
        );
        final MixedSqlNode mixedSqlNode = new MixedSqlNode(
                List.of(
                        new StaticTextSqlNode(interval.getText() + operate.getFormat().formatted(fieldPartParameter.finalValue())),
                        forEachSqlNode
                )
        );
        return buildSqlNodeCheckNull(mixedSqlNode, fieldPartParameter, valueParameter);
    }
}
