package com.github.developframework.mybatis.extension.core.parser.naming;

import com.github.developframework.mybatis.extension.core.annotation.Dynamic;
import com.github.developframework.mybatis.extension.core.annotation.SqlCustomized;
import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import com.github.developframework.mybatis.extension.core.utils.MybatisUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author qiushui on 2023-09-01.
 */
@Getter
@RequiredArgsConstructor
public class FieldNamingElement implements NamingElement {

    private final ColumnDefinition columnDefinition;

    private final Operate operate;

    /**
     * 构建SqlNode
     *
     * @param configuration          配置
     * @param interval               前一项的分隔符 AND 或 OR
     * @param namingMethodParameters 所用参数名
     * @param i                      所用参数的序号
     */
    public SqlNode buildSqlNode(Configuration configuration, Interval interval, Method method, List<NamingMethodParameter> namingMethodParameters, int i) {
        final String separator = interval == null ? "" : interval.getText();
        final String column;
        final String param;
        if (namingMethodParameters.isEmpty()) {
            column = column(null);
            param = null;
        } else {
            final NamingMethodParameter parameter = namingMethodParameters.get(i - 1);
            column = column(parameter);
            param = parameter.getKey();
        }

        final boolean dynamic = method.isAnnotationPresent(Dynamic.class);
        return switch (operate) {
            default -> {
                final TextSqlNode textSqlNode = new TextSqlNode(separator + String.format(operate.getFormat(), column, columnDefinition.getColumnMybatisPlaceholder().placeholder(param)));
                if (dynamic) {
                    yield new IfSqlNode(textSqlNode, param + " neq null");
                } else {
                    yield textSqlNode;
                }
            }
            case EQ, NE -> {
                final TextSqlNode textSqlNode = new TextSqlNode(separator + String.format(operate.getFormat(), column, columnDefinition.getColumnMybatisPlaceholder().placeholder(param)));
                if (dynamic) {
                    yield new IfSqlNode(textSqlNode, param + " neq null");
                } else {
                    final IfSqlNode ifSqlNode = new IfSqlNode(textSqlNode, param + " neq null");
                    final TextSqlNode defaultSqlNode = new TextSqlNode(separator + String.format((operate == Operate.EQ ? Operate.ISNULL : Operate.NOTNULL).getFormat(), column));
                    yield new ChooseSqlNode(List.of(ifSqlNode), defaultSqlNode);
                }
            }
            case ISNULL, NOTNULL, EQ_TRUE, EQ_FALSE -> {
                String text = String.format(operate.getFormat(), column);
                yield new TextSqlNode(separator + text);
            }
            case IN, NOT_IN -> {
                final String collectionExpression = MybatisUtils.getCollectionExpression(method, param);
                final SqlNode inSqlNode = inSqlNode(configuration, separator, collectionExpression, operate.getFormat());
                if (dynamic) {
                    yield new IfSqlNode(inSqlNode, collectionExpression + " neq null");
                } else {
                    yield inSqlNode;
                }
            }
            case BETWEEN -> {
                final String nextParam = namingMethodParameters.get(i).getKey();
                final String leftParam = columnDefinition.getColumnMybatisPlaceholder().placeholder(param);
                final String rightParam = columnDefinition.getColumnMybatisPlaceholder().placeholder(nextParam);
                // param > BETWEEN #{} AND #{}
                final IfSqlNode ifSqlNode1 = new IfSqlNode(
                        new TextSqlNode(separator + String.format(operate.getFormat(), column, leftParam, rightParam)),
                        String.format("%s neq null and %s neq null", param, nextParam)
                );
                // param >= #{}
                final IfSqlNode ifSqlNode2 = new IfSqlNode(
                        new TextSqlNode(separator + String.format(Operate.GTE.getFormat(), column, leftParam)),
                        String.format("%s neq null", param)
                );
                // param <= #{}
                final IfSqlNode ifSqlNode3 = new IfSqlNode(
                        new TextSqlNode(separator + String.format(Operate.LTE.getFormat(), column, rightParam)),
                        String.format("%s neq null", nextParam)
                );
                yield new ChooseSqlNode(List.of(ifSqlNode1, ifSqlNode2, ifSqlNode3), null);
            }
        };
    }

    private SqlNode inSqlNode(Configuration configuration, String separator, String collectionExpression, String format) {
        final String text = separator + String.format(format, columnDefinition.wrapColumn());
        final ForEachSqlNode forEachSqlNode = new ForEachSqlNode(
                configuration,
                new TextSqlNode("#{item}"),
                collectionExpression,
                false,
                null,
                "item",
                "(",
                ")",
                ","
        );
        return new MixedSqlNode(List.of(new TextSqlNode(text), forEachSqlNode));
    }

    private String column(NamingMethodParameter parameter) {
        if (parameter != null) {
            final SqlCustomized sqlCustomized = parameter.getSqlCustomized();
            if (sqlCustomized != null) {
                final String function = sqlCustomized.value();
                return function.isEmpty() ? columnDefinition.wrapColumn() : String.format(function, columnDefinition.wrapColumn());
            }
        }
        return columnDefinition.wrapColumn();
    }
}
