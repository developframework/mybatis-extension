package com.github.developframework.mybatis.extension.core;

import com.github.developframework.mybatis.extension.core.parser.MapperMethodParser;
import com.github.developframework.mybatis.extension.core.parser.def.BaseMapperDefaultParser;
import com.github.developframework.mybatis.extension.core.parser.naming.MapperMethodNamingParser;
import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
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
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author qiushui on 2023-09-01.
 */
public class MapperExtensionBuilder {

    private final Configuration configuration;
    private final MapperBuilderAssistant assistant;
    private final Class<?> type;
    private final EntityDefinition entityDefinition;
    private final MappedStatementMetadataRegistry mappedStatementMetadataRegistry;

    public MapperExtensionBuilder(Configuration configuration, Class<?> type, EntityDefinition entityDefinition, MappedStatementMetadataRegistry mappedStatementMetadataRegistry) {
        String resource = type.getName().replace('.', '/') + ".java (best guess)";
        this.assistant = new MapperBuilderAssistant(configuration, resource);
        this.configuration = configuration;
        this.type = type;
        this.entityDefinition = entityDefinition;
        this.mappedStatementMetadataRegistry = mappedStatementMetadataRegistry;
    }

    public void parse() {
        assistant.setCurrentNamespace(type.getName());
        for (Method method : type.getMethods()) {
            if (!canHaveStatement(method)) {
                continue;
            }
            String mappedStatementId = type.getName() + "." + method.getName();
            if (mappedStatementMetadataRegistry.exists(mappedStatementId)) {
                continue;
            }
            MappedStatement mappedStatement = parseStatement(method, mappedStatementId);
            if (mappedStatement != null) {
                mappedStatementMetadataRegistry.register(mappedStatement);
            }
        }
    }

    private boolean canHaveStatement(Method method) {
        // issue #237
        return !method.isBridge() && !method.isDefault();
    }

    private MappedStatement parseStatement(Method method, String mappedStatementId) {
        final Class<?> parameterTypeClass = getParameterType(method);
        final LanguageDriver languageDriver = getLanguageDriver(method);

        MapperMethodParseWrapper wrapper = buildWrapper(method);
        if (wrapper == null) {
            return null;
        }
        final SqlSource sqlSource = wrapper.sqlSource();
        final SqlCommandType sqlCommandType = wrapper.sqlCommandType();

        if (sqlCommandType == SqlCommandType.SELECT) {
            parseResultMap(method);
        }

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
                // 设定自增主键回填
                final ColumnDefinition primaryKeyColumnDefinition = entityDefinition.getPrimaryKeyColumnDefinitions()[0];
                keyProperty = primaryKeyColumnDefinition.getProperty();
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

        return assistant.addMappedStatement(
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

    private String parseResultMap(Method method) {
        Class<?> returnType = getReturnType(method);
        Arg[] args = method.getAnnotationsByType(Arg.class);
        Result[] results = method.getAnnotationsByType(Result.class);
        TypeDiscriminator typeDiscriminator = method.getAnnotation(TypeDiscriminator.class);
        String resultMapId = generateResultMapName(method);
        applyResultMap(resultMapId, returnType, args, results, typeDiscriminator);
        return resultMapId;
    }

    private void applyResultMap(String resultMapId, Class<?> returnType, Arg[] args, Result[] results, TypeDiscriminator discriminator) {
        List<ResultMapping> resultMappings = new ArrayList<>();
        applyConstructorArgs(args, returnType, resultMappings);
        applyResults(results, returnType, resultMappings);
        Discriminator disc = applyDiscriminator(resultMapId, returnType, discriminator);
        // TODO add AutoMappingBehaviour
        assistant.addResultMap(resultMapId, returnType, null, disc, resultMappings, null);
        createDiscriminatorResultMaps(resultMapId, returnType, discriminator);
    }

    private void applyConstructorArgs(Arg[] args, Class<?> resultType, List<ResultMapping> resultMappings) {
        for (Arg arg : args) {
            List<ResultFlag> flags = new ArrayList<>();
            flags.add(ResultFlag.CONSTRUCTOR);
            if (arg.id()) {
                flags.add(ResultFlag.ID);
            }
            @SuppressWarnings("unchecked")
            Class<? extends TypeHandler<?>> typeHandler = (Class<? extends TypeHandler<?>>)
                    (arg.typeHandler() == UnknownTypeHandler.class ? null : arg.typeHandler());
            ResultMapping resultMapping = assistant.buildResultMapping(
                    resultType,
                    nullOrEmpty(arg.name()),
                    nullOrEmpty(arg.column()),
                    arg.javaType() == void.class ? null : arg.javaType(),
                    arg.jdbcType() == JdbcType.UNDEFINED ? null : arg.jdbcType(),
                    nullOrEmpty(arg.select()),
                    nullOrEmpty(arg.resultMap()),
                    null,
                    nullOrEmpty(arg.columnPrefix()),
                    typeHandler,
                    flags,
                    null,
                    null,
                    false);
            resultMappings.add(resultMapping);
        }
    }

    private void applyResults(Result[] results, Class<?> resultType, List<ResultMapping> resultMappings) {
        for (Result result : results) {
            List<ResultFlag> flags = new ArrayList<>();
            if (result.id()) {
                flags.add(ResultFlag.ID);
            }
            @SuppressWarnings("unchecked")
            Class<? extends TypeHandler<?>> typeHandler = (Class<? extends TypeHandler<?>>)
                    ((result.typeHandler() == UnknownTypeHandler.class) ? null : result.typeHandler());
            boolean hasNestedResultMap = hasNestedResultMap(result);
            ResultMapping resultMapping = assistant.buildResultMapping(
                    resultType,
                    nullOrEmpty(result.property()),
                    nullOrEmpty(result.column()),
                    result.javaType() == void.class ? null : result.javaType(),
                    result.jdbcType() == JdbcType.UNDEFINED ? null : result.jdbcType(),
                    hasNestedSelect(result) ? nestedSelectId(result) : null,
                    hasNestedResultMap ? nestedResultMapId(result) : null,
                    null,
                    hasNestedResultMap ? findColumnPrefix(result) : null,
                    typeHandler,
                    flags,
                    null,
                    null,
                    isLazy(result));
            resultMappings.add(resultMapping);
        }
    }

    private boolean hasNestedResultMap(Result result) {
        if (result.one().resultMap().length() > 0 && result.many().resultMap().length() > 0) {
            throw new BuilderException("Cannot use both @One and @Many annotations in the same @Result");
        }
        return result.one().resultMap().length() > 0 || result.many().resultMap().length() > 0;
    }

    private boolean hasNestedSelect(Result result) {
        if (result.one().select().length() > 0 && result.many().select().length() > 0) {
            throw new BuilderException("Cannot use both @One and @Many annotations in the same @Result");
        }
        return result.one().select().length() > 0 || result.many().select().length() > 0;
    }

    private String nestedResultMapId(Result result) {
        String resultMapId = result.one().resultMap();
        if (resultMapId.length() < 1) {
            resultMapId = result.many().resultMap();
        }
        if (!resultMapId.contains(".")) {
            resultMapId = type.getName() + "." + resultMapId;
        }
        return resultMapId;
    }

    private String nestedSelectId(Result result) {
        String nestedSelect = result.one().select();
        if (nestedSelect.length() < 1) {
            nestedSelect = result.many().select();
        }
        if (!nestedSelect.contains(".")) {
            nestedSelect = type.getName() + "." + nestedSelect;
        }
        return nestedSelect;
    }

    private String findColumnPrefix(Result result) {
        String columnPrefix = result.one().columnPrefix();
        if (columnPrefix.length() < 1) {
            columnPrefix = result.many().columnPrefix();
        }
        return columnPrefix;
    }

    private boolean isLazy(Result result) {
        boolean isLazy = configuration.isLazyLoadingEnabled();
        if (result.one().select().length() > 0 && FetchType.DEFAULT != result.one().fetchType()) {
            isLazy = result.one().fetchType() == FetchType.LAZY;
        } else if (result.many().select().length() > 0 && FetchType.DEFAULT != result.many().fetchType()) {
            isLazy = result.many().fetchType() == FetchType.LAZY;
        }
        return isLazy;
    }

    private void createDiscriminatorResultMaps(String resultMapId, Class<?> resultType, TypeDiscriminator discriminator) {
        if (discriminator != null) {
            for (Case c : discriminator.cases()) {
                String caseResultMapId = resultMapId + "-" + c.value();
                List<ResultMapping> resultMappings = new ArrayList<>();
                // issue #136
                applyConstructorArgs(c.constructArgs(), resultType, resultMappings);
                applyResults(c.results(), resultType, resultMappings);
                // TODO add AutoMappingBehaviour
                assistant.addResultMap(caseResultMapId, c.type(), resultMapId, null, resultMappings, null);
            }
        }
    }

    private Discriminator applyDiscriminator(String resultMapId, Class<?> resultType, TypeDiscriminator discriminator) {
        if (discriminator != null) {
            String column = discriminator.column();
            Class<?> javaType = discriminator.javaType() == void.class ? String.class : discriminator.javaType();
            JdbcType jdbcType = discriminator.jdbcType() == JdbcType.UNDEFINED ? null : discriminator.jdbcType();
            @SuppressWarnings("unchecked")
            Class<? extends TypeHandler<?>> typeHandler = (Class<? extends TypeHandler<?>>)
                    (discriminator.typeHandler() == UnknownTypeHandler.class ? null : discriminator.typeHandler());
            Case[] cases = discriminator.cases();
            Map<String, String> discriminatorMap = new HashMap<>();
            for (Case c : cases) {
                String value = c.value();
                String caseResultMapId = resultMapId + "-" + value;
                discriminatorMap.put(value, caseResultMapId);
            }
            return assistant.buildDiscriminator(resultType, column, javaType, jdbcType, typeHandler, discriminatorMap);
        }
        return null;
    }


    private MapperMethodParseWrapper buildWrapper(Method method) {
        MapperMethodParseWrapper wrapper = buildBaseMapperDefault(method);
        if (wrapper == null) {
            // 通过命名方式生成SqlSource
            wrapper = buildByNaming(method);
        }
        return wrapper;
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

    private MapperMethodParseWrapper buildBaseMapperDefault(Method method) {
        BaseMapperDefaultParser parser = new BaseMapperDefaultParser(configuration);
        return parser.parse(entityDefinition, method);
    }

    private MapperMethodParseWrapper buildByNaming(Method method) {
        MapperMethodParser parser = new MapperMethodNamingParser(configuration);
        return parser.parse(entityDefinition, method);
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