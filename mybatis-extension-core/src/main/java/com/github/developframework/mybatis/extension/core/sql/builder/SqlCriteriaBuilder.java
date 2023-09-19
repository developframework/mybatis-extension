package com.github.developframework.mybatis.extension.core.sql.builder;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.parser.naming.Operate;
import com.github.developframework.mybatis.extension.core.sql.FieldSqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.MixedSqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.SqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.SqlFieldPart;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.ParameterKeys;
import com.github.developframework.mybatis.extension.core.utils.NameUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * @author qiushui on 2023-09-15.
 */
@RequiredArgsConstructor
public class SqlCriteriaBuilder {

    private final Configuration configuration;

    private final EntityDefinition entityDefinition;

    @Getter
    private final MapperMethod.ParamMap<Object> criteriaParamMap = new MapperMethod.ParamMap<>();

    private int criteriaParamIndex;

    @SneakyThrows(NoSuchFieldException.class)
    private Field getField(String valueProperty) {
        return entityDefinition.getEntityClass().getDeclaredField(valueProperty);
    }

    public SqlCriteria and(SqlCriteria... criteriaChain) {
        if (criteriaChain.length == 1) {
            return criteriaChain[0];
        } else {
            return new MixedSqlCriteria(configuration, Interval.AND, criteriaChain);
        }
    }

    public SqlCriteria or(SqlCriteria... criteriaChain) {
        if (criteriaChain.length == 1) {
            return criteriaChain[0];
        } else {
            return new MixedSqlCriteria(configuration, Interval.OR, criteriaChain);
        }
    }

    public SqlCriteria eq(SqlFieldPart fieldPart, Object value) {
        return simpleCommon(fieldPart, value, Operate.EQ);
    }

    public SqlCriteria ne(SqlFieldPart fieldPart, Object value) {
        return simpleCommon(fieldPart, value, Operate.NE);
    }

    public SqlCriteria gt(SqlFieldPart fieldPart, Object value) {
        return simpleCommon(fieldPart, value, Operate.GT);
    }

    public SqlCriteria gte(SqlFieldPart fieldPart, Object value) {
        return simpleCommon(fieldPart, value, Operate.GTE);
    }

    public SqlCriteria lt(SqlFieldPart fieldPart, Object value) {
        return simpleCommon(fieldPart, value, Operate.LT);
    }

    public SqlCriteria lte(SqlFieldPart fieldPart, Object value) {
        return simpleCommon(fieldPart, value, Operate.LTE);
    }

    public SqlCriteria isNull(SqlFieldPart fieldPart) {
        return simpleCommonWithNull(fieldPart, Operate.ISNULL);
    }

    public SqlCriteria isNotNull(SqlFieldPart fieldPart) {
        return simpleCommonWithNull(fieldPart, Operate.NOTNULL);
    }

    public SqlCriteria like(SqlFieldPart fieldPart, String value) {
        return simpleCommon(fieldPart, value, Operate.LIKE);
    }

    public SqlCriteria likeHead(SqlFieldPart fieldPart, String value) {
        return simpleCommon(fieldPart, value, Operate.LIKE_HEAD);
    }

    public SqlCriteria likeTail(SqlFieldPart fieldPart, String value) {
        return simpleCommon(fieldPart, value, Operate.LIKE_TAIL);
    }

    public SqlCriteria in(SqlFieldPart fieldPart, Collection<?> collection) {
        return commonWithIn(fieldPart, collection, Operate.IN);
    }

    public <T> SqlCriteria in(SqlFieldPart fieldPart, T... array) {
        return commonWithIn(fieldPart, array, Operate.IN);
    }

    public SqlCriteria notIn(SqlFieldPart fieldPart, Collection<?> collection) {
        return commonWithIn(fieldPart, collection, Operate.NOT_IN);
    }

    public <T> SqlCriteria notIn(SqlFieldPart fieldPart, T... array) {
        return commonWithIn(fieldPart, array, Operate.NOT_IN);
    }

    public SqlCriteria between(SqlFieldPart fieldPart, Object value1, Object value2) {
        return new FieldSqlCriteria(configuration) {
            @Override
            public Function<Interval, SqlNode> toSqlNode() {
                return interval -> {
                    final String paramName1, finalValue1, paramName2, finalValue2;
                    if (value1 == null) {
                        paramName1 = null;
                        finalValue1 = null;
                    } else if (value1 instanceof SqlFieldPart sfp) {
                        paramName1 = null;
                        finalValue1 = sfp.toSql();
                    } else {
                        paramName1 = collectParam(value1);
                        finalValue1 = NameUtils.placeholder(paramName1);
                    }
                    if (value2 == null) {
                        paramName2 = null;
                        finalValue2 = null;
                    } else if (value2 instanceof SqlFieldPart sfp) {
                        paramName2 = null;
                        finalValue2 = sfp.toSql();
                    } else {
                        paramName2 = collectParam(value2);
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
        };
    }

    private SqlCriteria simpleCommon(SqlFieldPart fieldPart, Object value, Operate operate) {
        return new FieldSqlCriteria(configuration) {
            @Override
            public Function<Interval, SqlNode> toSqlNode() {
                return interval -> {
                    final String paramName, finalValue;
                    if (value instanceof SqlFieldPart sfp) {
                        paramName = null;
                        finalValue = sfp.toSql();
                    } else {
                        paramName = collectParam(value);
                        finalValue = NameUtils.placeholder(paramName);
                    }
                    StaticTextSqlNode staticTextSqlNode = new StaticTextSqlNode(
                            interval.getText() + operate.getFormat().formatted(fieldPart.toSql(), finalValue)
                    );
                    return buildIfSqlNode(paramName, fieldPart, staticTextSqlNode);
                };
            }
        };
    }

    private SqlCriteria simpleCommonWithNull(SqlFieldPart fieldPart, Operate operate) {
        return new FieldSqlCriteria(configuration) {
            @Override
            public Function<Interval, SqlNode> toSqlNode() {
                return interval -> new StaticTextSqlNode(
                        interval.getText() + operate.getFormat().formatted(fieldPart.toSql())
                );
            }
        };
    }

    private SqlCriteria commonWithIn(SqlFieldPart fieldPart, Object value, Operate operate) {
        return new FieldSqlCriteria(configuration) {
            @Override
            public Function<Interval, SqlNode> toSqlNode() {
                return interval -> {
                    final String paramName, itemName;
                    paramName = collectParam(value);
                    itemName = paramName + "_item";
                    ForEachSqlNode forEachSqlNode = new ForEachSqlNode(
                            configuration,
                            new StaticTextSqlNode(NameUtils.placeholder(itemName)),
                            paramName,
                            true,
                            null,
                            itemName,
                            "(",
                            ")",
                            ","
                    );
                    return new MixedSqlNode(
                            List.of(
                                    new StaticTextSqlNode(interval.getText() + operate.getFormat().formatted(fieldPart.toSql())),
                                    forEachSqlNode
                            )
                    );
                };
            }
        };
    }


    private String collectParam(Object value) {
        String paramName = ParameterKeys.CRITERIA_PARAM + criteriaParamIndex++;
        criteriaParamMap.put(paramName, value);
        return paramName;
    }
}
