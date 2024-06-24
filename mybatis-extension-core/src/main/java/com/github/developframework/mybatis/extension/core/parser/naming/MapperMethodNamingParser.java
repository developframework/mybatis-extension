package com.github.developframework.mybatis.extension.core.parser.naming;

import com.github.developframework.mybatis.extension.core.annotation.Lock;
import com.github.developframework.mybatis.extension.core.annotation.SqlCustomized;
import com.github.developframework.mybatis.extension.core.parser.MapperMethodParseException;
import com.github.developframework.mybatis.extension.core.parser.MapperMethodParser;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaAssembler;
import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MapperMethodParseWrapper;
import com.github.developframework.mybatis.extension.core.structs.Pager;
import com.github.developframework.mybatis.extension.core.utils.MybatisUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 方法命名解析器
 *
 * @author qiushui on 2023-08-30.
 */
@RequiredArgsConstructor
public class MapperMethodNamingParser implements MapperMethodParser {

    private final Configuration configuration;

    private static final Set<String> OPERATES = Operate.keywords();

    /**
     * 方法名预设前缀
     */
    @Getter
    @RequiredArgsConstructor
    public enum MethodPrefix {

        SELECT_BY("selectBy", SqlCommandType.SELECT, "SELECT * FROM "),
        FIND_BY("findBy", SqlCommandType.SELECT, "SELECT * FROM "),
        QUERY_BY("queryBy", SqlCommandType.SELECT, "SELECT * FROM "),
        EXISTS_BY("existsBy", SqlCommandType.SELECT, "SELECT 1 FROM "),
        HAS_BY("hasBy", SqlCommandType.SELECT, "SELECT 1 FROM "),
        COUNT_BY("countBy", SqlCommandType.SELECT, "SELECT COUNT(*) FROM "),
        DELETE_BY("deleteBy", SqlCommandType.DELETE, "DELETE FROM "),
        REMOVE_BY("removeBy", SqlCommandType.DELETE, "DELETE FROM "),
        INSERT("insert", SqlCommandType.INSERT, "INSERT INTO "),
        INSERT_IGNORE("insertIgnore", SqlCommandType.INSERT, "INSERT IGNORE INTO "),
        REPLACE("replace", SqlCommandType.INSERT, "REPLACE INTO "),
        UPDATE("update", SqlCommandType.UPDATE, "UPDATE ");

        private final String keyword;

        private final SqlCommandType sqlCommandType;

        private final String sqlPrefix;
    }

    @Override
    public MapperMethodParseWrapper parse(EntityDefinition entityDefinition, Method method) {
        final String methodName = method.getName();
        final MethodPrefix methodPrefix = getMethodPrefix(methodName);
        if (methodPrefix == null) {
            return null;
        }

        final String subMethodName = methodName.substring(methodPrefix.getKeyword().length());
        final SqlCommandType sqlCommandType = methodPrefix.getSqlCommandType();
        final SqlSource sqlSource = switch (sqlCommandType) {
            case SELECT: {
                final String[] words = splitWord(subMethodName);
                final WhereSqlNode whereSqlNode = buildWhereSqlNode(method, words, entityDefinition);
                final TextSqlNode sqlNode = new TextSqlNode(methodPrefix.getSqlPrefix() + entityDefinition.wrapTableName());
                final List<SqlNode> sqlNodes = new LinkedList<>();
                sqlNodes.add(sqlNode);
                sqlNodes.add(whereSqlNode);
                final Lock lock = method.getAnnotation(Lock.class);
                if (lock != null) {
                    sqlNodes.add(new TextSqlNode(lock.value().getSql()));
                }
                // 查询存在优化处理
                if (methodPrefix == MethodPrefix.EXISTS_BY || methodPrefix == MethodPrefix.HAS_BY) {
                    sqlNodes.add(0, new StaticTextSqlNode("SELECT IFNULL(("));
                    sqlNodes.add(new StaticTextSqlNode("LIMIT 1), 0) `exists`"));
                }
                yield new DynamicSqlSource(configuration, new MixedSqlNode(sqlNodes));
            }
            case DELETE: {
                final String[] words = splitWord(subMethodName);
                final WhereSqlNode whereSqlNode = buildWhereSqlNode(method, words, entityDefinition);
                final TextSqlNode sqlNode = new TextSqlNode(methodPrefix.getSqlPrefix() + entityDefinition.wrapTableName());
                yield new DynamicSqlSource(configuration, new MixedSqlNode(List.of(sqlNode, whereSqlNode)));
            }
            case UPDATE: {
                final ColumnDefinition[] primaryKeyColumnDefinitions = entityDefinition.getPrimaryKeyColumnDefinitions();
                final StringBuilder sql = new StringBuilder(methodPrefix.getSqlPrefix())
                        .append(entityDefinition.wrapTableName())
                        .append(" SET ")
                        .append(
                                extractColumnDefinitions(method, entityDefinition, subMethodName)
                                        .stream()
                                        .map(cd -> String.format("%s = %s", cd.wrapColumn(), cd.placeholder()))
                                        .collect(Collectors.joining(", "))
                        )
                        .append(" WHERE ")
                        .append(
                                Stream
                                        .of(primaryKeyColumnDefinitions)
                                        .map(cd -> String.format("%s = %s", cd.wrapColumn(), cd.placeholder()))
                                        .collect(Collectors.joining(Interval.AND.getText()))
                        );
                yield new RawSqlSource(configuration, sql.toString(), entityDefinition.getEntityClass());
            }
            case INSERT: {
                final var columnDefinitions = extractColumnDefinitions(method, entityDefinition, subMethodName);
                final Collector<CharSequence, ?, String> joining = Collectors.joining(", ", "(", ")");
                final StringBuilder sql = new StringBuilder(methodPrefix.getSqlPrefix())
                        .append(entityDefinition.wrapTableName())
                        .append(columnDefinitions.stream().map(ColumnDefinition::wrapColumn).collect(joining))
                        .append(" VALUES");
                final String collectionExpression;
                try {
                    collectionExpression = MybatisUtils.getCollectionExpression(method, null);
                } catch (MapperMethodParseException e) {
                    // 单记录insert
                    sql.append(columnDefinitions.stream().map(ColumnDefinition::placeholder).collect(joining));
                    yield new RawSqlSource(configuration, sql.toString(), entityDefinition.getEntityClass());
                }
                if (collectionExpression == null) {
                    // 多参数单记录
                    final List<String> placeholders = new LinkedList<>();
                    final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                    for (int i = 0; i < parameterAnnotations.length; i++) {
                        String placeholder = String.format("#{%s}", ParamNameResolver.GENERIC_NAME_PREFIX + (i + 1));
                        if (parameterAnnotations[i].length != 0 && parameterAnnotations[i][0] instanceof Param) {
                            Param param = (Param) parameterAnnotations[i][0];
                            ColumnDefinition columnDefinition = entityDefinition.getColumnDefinitions().get(param.value());
                            if (columnDefinition != null) {
                                placeholder = columnDefinition.placeholder();
                            }
                        }
                        placeholders.add(placeholder);
                    }
                    sql.append(placeholders.stream().collect(joining));
                    yield new RawSqlSource(configuration, sql.toString(), entityDefinition.getEntityClass());
                } else {
                    // 批量insert
                    final ForEachSqlNode forEachSqlNode = new ForEachSqlNode(
                            configuration,
                            new TextSqlNode(columnDefinitions.stream().map(cd -> cd.getColumnMybatisPlaceholder().placeholder("it." + cd.getProperty())).collect(joining)),
                            collectionExpression,
                            false,
                            null,
                            "it",
                            null,
                            null,
                            ","
                    );
                    final MixedSqlNode mixedSqlNode = new MixedSqlNode(List.of(new TextSqlNode(sql.toString()), forEachSqlNode));
                    yield new DynamicSqlSource(configuration, mixedSqlNode);
                }
            }
            default:
                throw new AssertionError();
        };
        return new MapperMethodParseWrapper(sqlCommandType, sqlSource);
    }

    private MethodPrefix getMethodPrefix(String methodName) {
        for (MethodPrefix methodPrefix : MethodPrefix.values()) {
            if (methodName.startsWith(methodPrefix.getKeyword())) {
                return methodPrefix;
            }
        }
        return null;
    }

    /**
     * 根据分词构建WhereSqlNode
     */
    private WhereSqlNode buildWhereSqlNode(Method method, String[] words, EntityDefinition entityDefinition) {
        final List<NamingElement> namingElements = extractNamingElements(method.getName(), words, entityDefinition.getColumnDefinitions());
        final List<SqlNode> injectSqlNodes = autoInjectSqlNodes(entityDefinition);
        final List<SqlNode> sqlNodes = new LinkedList<>();
        if (!injectSqlNodes.isEmpty()) {
            sqlNodes.add(
                    new TrimSqlNode(
                            configuration,
                            new MixedSqlNode(injectSqlNodes),
                            " AND (",
                            "AND ",
                            ")",
                            null
                    )
            );
        }
        final List<SqlNode> assembleSqlNodes = assembleSqlNodes(namingElements, method);
        if (assembleSqlNodes.size() == 1) {
            sqlNodes.add(
                    new TrimSqlNode(
                            configuration,
                            new MixedSqlNode(assembleSqlNodes),
                            Interval.AND.getText(),
                            null,
                            null,
                            null
                    )
            );
        } else {
            sqlNodes.add(
                    new TrimSqlNode(
                            configuration,
                            new MixedSqlNode(assembleSqlNodes),
                            " AND (",
                            "AND ",
                            ")",
                            null
                    )
            );
        }
        return new WhereSqlNode(configuration, new MixedSqlNode(sqlNodes));
    }

    private List<SqlNode> autoInjectSqlNodes(EntityDefinition entityDefinition) {
        final List<SqlNode> sqlNodes = new LinkedList<>();
        if (entityDefinition.hasLogicDelete()) {
            sqlNodes.add(new StaticTextSqlNode(String.format(" AND %s = 0", entityDefinition.getLogicDeleteColumnDefinition().wrapColumn())));
        }
        if (entityDefinition.hasMultipleTenant()) {
            for (ColumnDefinition cd : entityDefinition.getMultipleTenantColumnDefinitions()) {
                final StaticTextSqlNode textSqlNode = new StaticTextSqlNode(
                        String.format(" AND %s = %s", cd.wrapColumn(), cd.placeholder())
                );
                sqlNodes.add(
                        new IfSqlNode(textSqlNode, cd.getProperty() + " neq null")
                );
            }
        }
        return sqlNodes;
    }

    /**
     * 分词
     */
    private String[] splitWord(String str) {
        StringBuilder sb = new StringBuilder();
        List<String> list = new LinkedList<>();
        final char[] chars = str.toCharArray();
        for (char c : chars) {
            if (c >= 'A' && c <= 'Z') {
                if (!sb.isEmpty()) {
                    list.add(sb.toString());
                }
                sb.setLength(0);
                sb.append((char) (c + 32));
            } else {
                sb.append(c);
            }
        }
        list.add(sb.toString());
        return list.toArray(String[]::new);
    }

    /**
     * 提取命名元素
     */
    private List<NamingElement> extractNamingElements(String method, String[] words, Map<String, ColumnDefinition> columnDefinitions) {
        List<NamingElement> namingElements = new LinkedList<>();
        List<String> buffer = new ArrayList<>();
        for (final String word : words) {
            if (word.equalsIgnoreCase(Interval.AND.name()) || word.equalsIgnoreCase(Interval.OR.name())) {
                namingElements.add(determineNamingElement(method, buffer, columnDefinitions));
                namingElements.add(new IntervalNamingElement(Interval.valueOf(word.toUpperCase())));
                buffer.clear();
            } else {
                buffer.add(word);
            }
        }
        if (!buffer.isEmpty()) {
            namingElements.add(determineNamingElement(method, buffer, columnDefinitions));
        }
        return namingElements;
    }

    /**
     * 决定一个NamingElement
     */
    private NamingElement determineNamingElement(String method, List<String> buffer, Map<String, ColumnDefinition> columnDefinitions) {
        String field = "";
        for (int i = 0, c = buffer.size(); i < c; i++) {
            field = concatWord(field, buffer.get(i));
            String operateStr = "";
            for (int j = i + 1; j < c; j++) {
                operateStr = concatWord(operateStr, buffer.get(j));
            }
            ColumnDefinition columnDefinition = columnDefinitions.get(field);
            if (columnDefinition != null) {
                if (operateStr.isEmpty()) {
                    operateStr = Operate.EQ.getKeyword();
                }
                if (OPERATES.contains(operateStr)) {
                    return new FieldNamingElement(columnDefinition, Operate.of(operateStr));
                }
            }
        }
        throw new MapperMethodParseException("方法“" + method + "”命名错误");
    }

    /**
     * 拼接单词
     */
    private String concatWord(String s1, String s2) {
        if (s1.isEmpty()) {
            return s2;
        }
        return s1 + (char) (s2.charAt(0) - 32) + s2.substring(1);
    }

    /**
     * 从方法名上提取字段信息
     */
    private List<ColumnDefinition> extractColumnDefinitions(Method method, EntityDefinition entityDefinition, String subMethodName) {
        final Map<String, ColumnDefinition> columnDefinitions = entityDefinition.getColumnDefinitions();
        final List<ColumnDefinition> list = new ArrayList<>();
        for (String methodName : subMethodName.split("_")) {
            final int length = methodName.length();
            int left = 0, right = length;
            // 错误字段的左右索引
            int L = left, R = right;
            while (left <= right) {
                if (left == right) {
                    throw new MapperMethodParseException("方法“" + method.getName() + "”命名错误：" + methodName.substring(L, R));
                } else if (right == length) {
                    String temp = Introspector.decapitalize(methodName.substring(left, right));
                    final var columnDefinition = columnDefinitions.get(temp);
                    if (columnDefinition != null) {
                        list.add(columnDefinition);
                        break;
                    } else {
                        L = left;
                        R = right;
                    }
                } else {
                    char c = methodName.charAt(right);
                    if (c >= 'A' && c <= 'Z') {
                        String temp = Introspector.decapitalize(methodName.substring(left, right));
                        final var columnDefinition = columnDefinitions.get(temp);
                        if (columnDefinition != null) {
                            list.add(columnDefinition);
                            left += temp.length();
                            right = length;
                            continue;
                        } else {
                            L = left;
                            R = right;
                        }
                    }
                }
                right--;
            }
        }
        return list;
    }

    private List<SqlNode> assembleSqlNodes(List<NamingElement> namingElements, Method method) {
        List<SqlNode> sqlNodes = new ArrayList<>();
        Interval interval = null;

        // 参数名列表 为了剔除Pager
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final List<NamingMethodParameter> namingMethodParameters = new ArrayList<>();
        for (int i = 0, c = method.getParameterCount(); i < c; i++) {
            if (parameterTypes[i] != Pager.class) {
                SqlCustomized sqlCustomized = null;
                for (Annotation annotation : method.getParameterAnnotations()[i]) {
                    if (annotation.annotationType() == SqlCustomized.class) {
                        sqlCustomized = (SqlCustomized) annotation;
                        break;
                    }
                }
                namingMethodParameters.add(new NamingMethodParameter(ParamNameResolver.GENERIC_NAME_PREFIX + (i + 1), sqlCustomized));
            } else if (parameterTypes[i] == SqlCriteriaAssembler.class) {
                throw new MapperMethodParseException("命名方式和SqlCriteriaAssembler不能同时使用");
            }
        }
        int i = 1;
        for (NamingElement namingElement : namingElements) {
            if (namingElement instanceof IntervalNamingElement) {
                IntervalNamingElement intervalNamingElement = (IntervalNamingElement) namingElement;
                interval = intervalNamingElement.getInterval();
            } else {
                final FieldNamingElement fieldNamingElement = (FieldNamingElement) namingElement;
                sqlNodes.add(fieldNamingElement.buildSqlNode(configuration, interval, method, namingMethodParameters, i));
                i += fieldNamingElement.getOperate().getParamCount();
                interval = null;
            }
        }
        return sqlNodes;
    }

}
