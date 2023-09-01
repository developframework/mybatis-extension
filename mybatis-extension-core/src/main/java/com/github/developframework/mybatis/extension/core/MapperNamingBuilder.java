package com.github.developframework.mybatis.extension.core;

import com.github.developframework.mybatis.extension.core.parser.MapperMethodParser;
import com.github.developframework.mybatis.extension.core.parser.naming.MapperMethodNamingParser;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MapperMethodParseWrapper;
import lombok.Getter;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author qiushui on 2023-09-01.
 */
public class MapperNamingBuilder {

    private final Configuration configuration;
    private final MapperBuilderAssistant assistant;
    private final Class<?> type;
    private final EntityDefinition entityDefinition;

    public MapperNamingBuilder(Configuration configuration, Class<?> type, EntityDefinition entityDefinition) {
        String resource = type.getName().replace('.', '/') + ".java (best guess)";
        this.assistant = new MapperBuilderAssistant(configuration, resource);
        this.configuration = configuration;
        this.type = type;
        this.entityDefinition = entityDefinition;
    }

    public void parse() {
        final Set<String> existsMappedStatementIds = configuration.getMappedStatements().stream().map(MappedStatement::getId).collect(Collectors.toSet());
        for (Method method : type.getMethods()) {
            if (!canHaveStatement(method)) {
                continue;
            }
            String mappedStatementId = type.getName() + "." + method.getName();
            if (existsMappedStatementIds.contains(mappedStatementId)) {
                continue;
            }
            parseStatement(method, mappedStatementId);
        }
    }

    private boolean canHaveStatement(Method method) {
        // issue #237
        return !method.isBridge() && !method.isDefault();
    }

    private void parseStatement(Method method, String mappedStatementId) {
        final Class<?> parameterTypeClass = getParameterType(method);
        final LanguageDriver languageDriver = getLanguageDriver(method);

        // 通过命名方式生成SqlSource
        final MapperMethodParseWrapper wrapper = buildByNaming(method);
        if (wrapper == null) {
            return;
        }
        final SqlSource sqlSource = wrapper.sqlSource();
        final SqlCommandType sqlCommandType = wrapper.sqlCommandType();
        final Options options = getAnnotationWrapper(method, false, Options.class).map(x -> (Options) x.getAnnotation()).orElse(null);
        final KeyGenerator keyGenerator;
        String keyProperty = null;
        String keyColumn = null;
        if (SqlCommandType.INSERT.equals(sqlCommandType) || SqlCommandType.UPDATE.equals(sqlCommandType)) {
            // first check for SelectKey annotation - that overrides everything else
            SelectKey selectKey = getAnnotationWrapper(method, false, SelectKey.class).map(x -> (SelectKey) x.getAnnotation()).orElse(null);
            if (selectKey != null) {
                keyGenerator = handleSelectKeyAnnotation(selectKey, mappedStatementId, getParameterType(method), languageDriver);
                keyProperty = selectKey.keyProperty();
            } else if (options == null) {
                keyGenerator = configuration.isUseGeneratedKeys() ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
            } else {
                keyGenerator = options.useGeneratedKeys() ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
                keyProperty = options.keyProperty();
                keyColumn = options.keyColumn();
            }
        } else {
            keyGenerator = NoKeyGenerator.INSTANCE;
        }

        Integer fetchSize = null;
        Integer timeout = null;
        StatementType statementType = StatementType.PREPARED;
        ResultSetType resultSetType = configuration.getDefaultResultSetType();
        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
        boolean flushCache = !isSelect;
        boolean useCache = isSelect;
        if (options != null) {
            if (Options.FlushCachePolicy.TRUE.equals(options.flushCache())) {
                flushCache = true;
            } else if (Options.FlushCachePolicy.FALSE.equals(options.flushCache())) {
                flushCache = false;
            }
            useCache = options.useCache();
            fetchSize = options.fetchSize() > -1 || options.fetchSize() == Integer.MIN_VALUE ? options.fetchSize() : null; //issue #348
            timeout = options.timeout() > -1 ? options.timeout() : null;
            statementType = options.statementType();
            if (options.resultSetType() != ResultSetType.DEFAULT) {
                resultSetType = options.resultSetType();
            }
        }

        String resultMapId = null;
        if (isSelect) {
            ResultMap resultMapAnnotation = method.getAnnotation(ResultMap.class);
            if (resultMapAnnotation != null) {
                resultMapId = String.join(",", resultMapAnnotation.value());
            } else {
                resultMapId = generateResultMapName(method);
            }
        }

        assistant.addMappedStatement(
                mappedStatementId,
                sqlSource,
                statementType,
                sqlCommandType,
                fetchSize,
                timeout,
                // ParameterMapID
                null,
                parameterTypeClass,
                resultMapId,
                getReturnType(method),
                resultSetType,
                flushCache,
                useCache,
                // TODO gcode issue #577
                false,
                keyGenerator,
                keyProperty,
                keyColumn,
                options != null ? options.databaseId() : "",
                languageDriver,
                // ResultSets
                options != null ? nullOrEmpty(options.resultSets()) : null
        );
    }

    private Class<?> getParameterType(Method method) {
        Class<?> parameterType = null;
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> currentParameterType : parameterTypes) {
            if (!RowBounds.class.isAssignableFrom(currentParameterType) && !ResultHandler.class.isAssignableFrom(currentParameterType)) {
                if (parameterType == null) {
                    parameterType = currentParameterType;
                } else {
                    // issue #135
                    parameterType = MapperMethod.ParamMap.class;
                }
            }
        }
        return parameterType;
    }

    private LanguageDriver getLanguageDriver(Method method) {
        Lang lang = method.getAnnotation(Lang.class);
        Class<? extends LanguageDriver> langClass = null;
        if (lang != null) {
            langClass = lang.value();
        }
        return configuration.getLanguageDriver(langClass);
    }

    @SafeVarargs
    private Optional<NamingAnnotationWrapper> getAnnotationWrapper(Method method, boolean errorIfNoMatch, Class<? extends Annotation>... targetTypes) {
        return getAnnotationWrapper(method, errorIfNoMatch, Arrays.asList(targetTypes));
    }

    private Optional<NamingAnnotationWrapper> getAnnotationWrapper(Method method, boolean errorIfNoMatch, Collection<Class<? extends Annotation>> targetTypes) {
        String databaseId = configuration.getDatabaseId();
        Map<String, NamingAnnotationWrapper> statementAnnotations = targetTypes.stream()
                .flatMap(x -> Arrays.stream(method.getAnnotationsByType(x))).map(NamingAnnotationWrapper::new)
                .collect(Collectors.toMap(NamingAnnotationWrapper::getDatabaseId, x -> x, (existing, duplicate) -> {
                    throw new BuilderException(String.format("Detected conflicting annotations '%s' and '%s' on '%s'.",
                            existing.getAnnotation(), duplicate.getAnnotation(),
                            method.getDeclaringClass().getName() + "." + method.getName()));
                }));
        NamingAnnotationWrapper annotationWrapper = null;
        if (databaseId != null) {
            annotationWrapper = statementAnnotations.get(databaseId);
        }
        if (annotationWrapper == null) {
            annotationWrapper = statementAnnotations.get("");
        }
        if (errorIfNoMatch && annotationWrapper == null && !statementAnnotations.isEmpty()) {
            // Annotations exist, but there is no matching one for the specified databaseId
            throw new BuilderException(
                    String.format(
                            "Could not find a statement annotation that correspond a current database or default statement on method '%s.%s'. Current database id is [%s].",
                            method.getDeclaringClass().getName(), method.getName(), databaseId));
        }
        return Optional.ofNullable(annotationWrapper);
    }

    private String nullOrEmpty(String value) {
        return value == null || value.trim().isEmpty() ? null : value;
    }

    private KeyGenerator handleSelectKeyAnnotation(SelectKey selectKeyAnnotation, String baseStatementId, Class<?> parameterTypeClass, LanguageDriver languageDriver) {
        String id = baseStatementId + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        Class<?> resultTypeClass = selectKeyAnnotation.resultType();
        StatementType statementType = selectKeyAnnotation.statementType();
        String keyProperty = selectKeyAnnotation.keyProperty();
        String keyColumn = selectKeyAnnotation.keyColumn();
        boolean executeBefore = selectKeyAnnotation.before();

        // defaults
        boolean useCache = false;
        KeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;
        Integer fetchSize = null;
        Integer timeout = null;
        boolean flushCache = false;
        String parameterMap = null;
        String resultMap = null;
        ResultSetType resultSetTypeEnum = null;
        String databaseId = selectKeyAnnotation.databaseId().isEmpty() ? null : selectKeyAnnotation.databaseId();

        SqlSource sqlSource = buildSqlSource(selectKeyAnnotation, parameterTypeClass, languageDriver, null);
        SqlCommandType sqlCommandType = SqlCommandType.SELECT;

        assistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType, fetchSize, timeout, parameterMap, parameterTypeClass, resultMap, resultTypeClass, resultSetTypeEnum,
                flushCache, useCache, false,
                keyGenerator, keyProperty, keyColumn, databaseId, languageDriver, null);

        id = assistant.applyCurrentNamespace(id, false);

        MappedStatement keyStatement = configuration.getMappedStatement(id, false);
        SelectKeyGenerator answer = new SelectKeyGenerator(keyStatement, executeBefore);
        configuration.addKeyGenerator(id, answer);
        return answer;
    }

    private MapperMethodParseWrapper buildByNaming(Method method) {
        MapperMethodParser mapperMethodParser = new MapperMethodNamingParser(configuration);
        return mapperMethodParser.parse(entityDefinition, method);
    }

    private SqlSource buildSqlSource(Annotation annotation, Class<?> parameterType, LanguageDriver languageDriver, Method method) {
        if (annotation instanceof Select) {
            return buildSqlSourceFromStrings(((Select) annotation).value(), parameterType, languageDriver);
        } else if (annotation instanceof Update) {
            return buildSqlSourceFromStrings(((Update) annotation).value(), parameterType, languageDriver);
        } else if (annotation instanceof Insert) {
            return buildSqlSourceFromStrings(((Insert) annotation).value(), parameterType, languageDriver);
        } else if (annotation instanceof Delete) {
            return buildSqlSourceFromStrings(((Delete) annotation).value(), parameterType, languageDriver);
        } else if (annotation instanceof SelectKey) {
            return buildSqlSourceFromStrings(((SelectKey) annotation).statement(), parameterType, languageDriver);
        }
        return new ProviderSqlSource(assistant.getConfiguration(), annotation, type, method);
    }

    private SqlSource buildSqlSourceFromStrings(String[] strings, Class<?> parameterTypeClass,
                                                LanguageDriver languageDriver) {
        return languageDriver.createSqlSource(configuration, String.join(" ", strings).trim(), parameterTypeClass);
    }

    private String generateResultMapName(Method method) {
        Results results = method.getAnnotation(Results.class);
        if (results != null && !results.id().isEmpty()) {
            return type.getName() + "." + results.id();
        }
        StringBuilder suffix = new StringBuilder();
        for (Class<?> c : method.getParameterTypes()) {
            suffix.append("-");
            suffix.append(c.getSimpleName());
        }
        if (suffix.isEmpty()) {
            suffix.append("-void");
        }
        return type.getName() + "." + method.getName() + suffix;
    }

    private Class<?> getReturnType(Method method) {
        Class<?> returnType = method.getReturnType();
        Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, type);
        if (resolvedReturnType instanceof Class) {
            returnType = (Class<?>) resolvedReturnType;
            if (returnType.isArray()) {
                returnType = returnType.getComponentType();
            }
            // gcode issue #508
            if (void.class.equals(returnType)) {
                ResultType rt = method.getAnnotation(ResultType.class);
                if (rt != null) {
                    returnType = rt.value();
                }
            }
        } else if (resolvedReturnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) resolvedReturnType;
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            if (Collection.class.isAssignableFrom(rawType) || Cursor.class.isAssignableFrom(rawType)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    Type returnTypeParameter = actualTypeArguments[0];
                    if (returnTypeParameter instanceof Class<?>) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        // (gcode issue #443) actual type can be a also a parameterized type
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    } else if (returnTypeParameter instanceof GenericArrayType) {
                        Class<?> componentType = (Class<?>) ((GenericArrayType) returnTypeParameter).getGenericComponentType();
                        // (gcode issue #525) support List<byte[]>
                        returnType = Array.newInstance(componentType, 0).getClass();
                    }
                }
            } else if (method.isAnnotationPresent(MapKey.class) && Map.class.isAssignableFrom(rawType)) {
                // (gcode issue 504) Do not look into Maps if there is not MapKey annotation
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 2) {
                    Type returnTypeParameter = actualTypeArguments[1];
                    if (returnTypeParameter instanceof Class<?>) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        // (gcode issue 443) actual type can be a also a parameterized type
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    }
                }
            } else if (Optional.class.equals(rawType)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                Type returnTypeParameter = actualTypeArguments[0];
                if (returnTypeParameter instanceof Class<?>) {
                    returnType = (Class<?>) returnTypeParameter;
                }
            }
        }

        return returnType;
    }

    @Getter
    private class NamingAnnotationWrapper {

        private final Annotation annotation;

        private final String databaseId;

        NamingAnnotationWrapper(Annotation annotation) {
            this.annotation = annotation;
            if (annotation instanceof Options) {
                databaseId = ((Options) annotation).databaseId();
            } else if (annotation instanceof SelectKey) {
                databaseId = ((SelectKey) annotation).databaseId();
            } else {
                databaseId = "";
            }
        }
    }

}
