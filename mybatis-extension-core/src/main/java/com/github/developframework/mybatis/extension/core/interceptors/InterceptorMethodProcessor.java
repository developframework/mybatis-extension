package com.github.developframework.mybatis.extension.core.interceptors;

import org.apache.ibatis.plugin.Invocation;

/**
 * 拦截器方法处理器
 *
 * @author qiushui on 2023-09-18.
 */
public interface InterceptorMethodProcessor {

    Object executorUpdate(Invocation invocation, InterceptContext context) throws Throwable;

    Object executorQuery(Invocation invocation, InterceptContext context) throws Throwable;

    Object statementHandlerPrepare(Invocation invocation, InterceptContext context) throws Throwable;

    Object statementHandlerQuery(Invocation invocation, InterceptContext context) throws Throwable;
}
