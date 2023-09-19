package com.github.developframework.mybatis.extension.core.interceptors;

import com.github.developframework.mybatis.extension.core.EntityDefinitionRegistry;
import com.github.developframework.mybatis.extension.core.MappedStatementMetadataRegistry;
import com.github.developframework.mybatis.extension.core.autoinject.AutoInjectProviderRegistry;
import com.github.developframework.mybatis.extension.core.structs.MappedStatementMetadata;
import lombok.Setter;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

/**
 * @author qiushui on 2023-09-18.
 */
@Intercepts({
        @Signature(
                type = Executor.class,
                method = "update",
                args = {MappedStatement.class, Object.class}
        ),
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
        ),
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}
        ),
        @Signature(
                type = StatementHandler.class,
                method = "prepare",
                args = {Connection.class, Integer.class}
        ),
        @Signature(
                type = StatementHandler.class,
                method = "query",
                args = {Statement.class, ResultHandler.class}
        )
})
@Setter
public class MybatisExtensionInterceptor implements Interceptor {

    private EntityDefinitionRegistry entityDefinitionRegistry;

    private MappedStatementMetadataRegistry mappedStatementMetadataRegistry;

    private AutoInjectProviderRegistry autoInjectProviderRegistry;

    private final InterceptorMethodProcessor interceptorMethodProcessor;

    public MybatisExtensionInterceptor(List<InnerInterceptor> innerInterceptorChain) {
        interceptorMethodProcessor = pluginAll(innerInterceptorChain, new DefaultInterceptorMethodProcessor());
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        final Object target = invocation.getTarget();
        if (target instanceof Executor) {
            return handleExecutor(invocation);
        } else if (target instanceof StatementHandler) {
            return handleStatementHandler(invocation);
        }
        return invocation.proceed();
    }

    private Object handleExecutor(Invocation invocation) throws Throwable {
        final MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        final MappedStatementMetadata metadata = mappedStatementMetadataRegistry.get(ms.getId());
        final Class<?> entityClass = metadata.getEntityClass();
        if (entityClass != null) {
            final InterceptContext context = new InterceptContext();
            context.setMappedStatementMetadata(metadata);
            context.setEntityDefinition(entityDefinitionRegistry.get(entityClass));
            context.setAutoInjectProviderRegistry(autoInjectProviderRegistry);
            switch (invocation.getMethod().getName()) {
                case "update": {
                    return interceptorMethodProcessor.executorUpdate(invocation, context);
                }
                case "query": {
                    return interceptorMethodProcessor.executorQuery(invocation, context);
                }
            }
        }
        return invocation.proceed();
    }

    private Object handleStatementHandler(Invocation invocation) throws Throwable {
        final InterceptContext context = new InterceptContext();
        context.setAutoInjectProviderRegistry(autoInjectProviderRegistry);
        return switch (invocation.getMethod().getName()) {
            case "prepare" -> interceptorMethodProcessor.statementHandlerPrepare(invocation, context);
            case "query" -> interceptorMethodProcessor.statementHandlerQuery(invocation, context);
            default -> invocation.proceed();
        };
    }

    private InterceptorMethodProcessor pluginAll(List<InnerInterceptor> innerInterceptorChain, InterceptorMethodProcessor processor) {
        for (InnerInterceptor innerInterceptor : innerInterceptorChain) {
            processor = innerInterceptor.plugin(processor);
        }
        return processor;
    }
}
