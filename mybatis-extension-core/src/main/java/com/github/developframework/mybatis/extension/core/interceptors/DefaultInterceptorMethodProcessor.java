package com.github.developframework.mybatis.extension.core.interceptors;

import org.apache.ibatis.plugin.Invocation;

/**
 * @author qiushui on 2023-09-18.
 */
public class DefaultInterceptorMethodProcessor implements InterceptorMethodProcessor {

    @Override
    public Object executorUpdate(Invocation invocation, InterceptContext context) throws Throwable {
        return invocation.proceed();
    }

    @Override
    public Object executorQuery(Invocation invocation, InterceptContext context) throws Throwable {
        return invocation.proceed();
    }

    @Override
    public Object statementHandlerPrepare(Invocation invocation, InterceptContext context) throws Throwable {
        return invocation.proceed();
    }

    @Override
    public Object statementHandlerQuery(Invocation invocation, InterceptContext context) throws Throwable {
        return invocation.proceed();
    }
}
