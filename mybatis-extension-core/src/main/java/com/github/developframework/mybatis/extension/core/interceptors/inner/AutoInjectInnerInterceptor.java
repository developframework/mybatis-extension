package com.github.developframework.mybatis.extension.core.interceptors.inner;

import com.github.developframework.mybatis.extension.core.MybatisExtensionException;
import com.github.developframework.mybatis.extension.core.autoinject.AutoInjectProvider;
import com.github.developframework.mybatis.extension.core.autoinject.AutoInjectProviderRegistry;
import com.github.developframework.mybatis.extension.core.interceptors.InnerInterceptor;
import com.github.developframework.mybatis.extension.core.interceptors.InnerInvocation;
import com.github.developframework.mybatis.extension.core.interceptors.InterceptContext;
import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.utils.MybatisUtils;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author qiushui on 2023-09-18.
 */
public class AutoInjectInnerInterceptor implements InnerInterceptor {

    @Override
    public Object executorQuery(InnerInvocation innerInvocation, InterceptContext context) throws Throwable {
        if (context.getEntityDefinition().hasAutoInject()) {
            final Object[] args = innerInvocation.getInvocation().getArgs();
            final MappedStatement mappedStatement = (MappedStatement) args[0];
            args[1] = queryParameterInject(mappedStatement, context, args[1]);
        }
        return innerInvocation.proceed();
    }

    @Override
    public Object executorUpdate(InnerInvocation innerInvocation, InterceptContext context) throws Throwable {
        if (context.getEntityDefinition().hasAutoInject()) {
            final MappedStatement mappedStatement = (MappedStatement) innerInvocation.getInvocation().getArgs()[0];
            final SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
            switch (sqlCommandType) {
                case INSERT:
                case UPDATE: {
                    final Object[] args = innerInvocation.getInvocation().getArgs();
                    entityInject(mappedStatement, context, args[1]);
                }
                break;
                case DELETE: {
                    // 和查询的一样
                    final Object[] args = innerInvocation.getInvocation().getArgs();
                    args[1] = queryParameterInject(mappedStatement, context, args[1]);
                }
            }
        }
        return innerInvocation.proceed();
    }

    /**
     * 实体注入
     */
    private void entityInject(MappedStatement ms, InterceptContext context, Object parameter) {
        final EntityDefinition entityDefinition = context.getEntityDefinition();
        final AutoInjectProviderRegistry autoInjectProviderRegistry = context.getAutoInjectProviderRegistry();
        // 注入值
        for (Object entity : MybatisUtils.findAll(parameter, entityDefinition.getEntityClass())) {
            if (entity != null) {
                for (ColumnDefinition columnDefinition : entityDefinition.getAutoInjectColumnDefinitions()) {
                    setValue(
                            entity,
                            entityDefinition,
                            columnDefinition,
                            ms.getSqlCommandType(),
                            autoInjectProviderRegistry.getAutoInjectProvider(columnDefinition.getAutoInjectProviderClass())
                    );
                }
            }
        }
    }

    /**
     * 替换parameter值，需要添加租户字段
     */
    @SuppressWarnings("unchecked")
    private Object queryParameterInject(MappedStatement ms, InterceptContext context, Object parameter) {
        final EntityDefinition entityDefinition = context.getEntityDefinition();
        if (parameter instanceof Map) {
            final MapperMethod.ParamMap<Object> newParameter = new MapperMethod.ParamMap<>();
            // 多个参数值 需要往原来的Map里添加租户值
            newParameter.putAll((Map<String, Object>) parameter);
            final AutoInjectProviderRegistry autoInjectProviderRegistry = context.getAutoInjectProviderRegistry();
            for (ColumnDefinition columnDefinition : entityDefinition.getMultipleTenantColumnDefinitions()) {
                final Object value = getProviderValue(autoInjectProviderRegistry, entityDefinition, columnDefinition, parameter);
                newParameter.put(columnDefinition.getProperty(), value);
            }
            return newParameter;
        } else {
            // 单个参数值 需要重新弄一个Map来添加租户值
            final BoundSql boundSql = ms.getBoundSql(parameter);
            final List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
            if (parameterMappings.isEmpty()) {
                return parameter;
            } else {
                final MapperMethod.ParamMap<Object> newParameter = new MapperMethod.ParamMap<>();
                final AutoInjectProviderRegistry autoInjectProviderRegistry = context.getAutoInjectProviderRegistry();
                // 正常 boundSql.getParameterMappings() 只会有 (1 + 租户字段数量) 个
                for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                    final String property = parameterMapping.getProperty();

                    ColumnDefinition matchColumnDefinition = null;
                    for (ColumnDefinition columnDefinition : entityDefinition.getMultipleTenantColumnDefinitions()) {
                        if (columnDefinition.getProperty().equals(property)) {
                            matchColumnDefinition = columnDefinition;
                            break;
                        }
                    }

                    final Object value = matchColumnDefinition == null ? parameter : getProviderValue(autoInjectProviderRegistry, entityDefinition, matchColumnDefinition, parameter);
                    newParameter.put(property, value);
                }
                return newParameter;
            }
        }
    }

    private Object getProviderValue(AutoInjectProviderRegistry autoInjectProviderRegistry, EntityDefinition entityDefinition, ColumnDefinition columnDefinition, Object parameter) {
        final Class<? extends AutoInjectProvider> autoInjectProviderClass = columnDefinition.getAutoInjectProviderClass();
        if (autoInjectProviderClass != null) {
            final AutoInjectProvider autoInjectProvider = autoInjectProviderRegistry.getAutoInjectProvider(autoInjectProviderClass);
            return autoInjectProvider.provide(entityDefinition, columnDefinition, parameter);
        }
        return null;
    }

    private void setValue(Object entity, EntityDefinition entityDefinition, ColumnDefinition columnDefinition, SqlCommandType sqlCommandType, AutoInjectProvider autoInjectProvider) {
        for (SqlCommandType commandType : autoInjectProvider.needInject()) {
            if (commandType == sqlCommandType) {
                final Class<?> entityClass = entity.getClass();
                try {
                    final Field field = MybatisUtils.getField(entityClass, columnDefinition.getProperty());
                    field.setAccessible(true);
                    Object value = field.get(entity);
                    // 原来没值
                    if (value == null) {
                        value = autoInjectProvider.provide(entityDefinition, columnDefinition, entity);
                        field.set(entity, value);
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new MybatisExtensionException(e.getMessage());
                }
                break;
            }
        }
    }
}
