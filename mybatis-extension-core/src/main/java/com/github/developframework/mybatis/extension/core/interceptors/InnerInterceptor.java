package com.github.developframework.mybatis.extension.core.interceptors;

/**
 * @author qiushui on 2023-09-18.
 */
public interface InnerInterceptor {

    default Object executorUpdate(InnerInvocation innerInvocation, InterceptContext context) throws Throwable {
        return innerInvocation.proceed();
    }

    default Object executorQuery(InnerInvocation innerInvocation, InterceptContext context) throws Throwable {
        return innerInvocation.proceed();
    }

    default Object statementHandlerPrepare(InnerInvocation innerInvocation, InterceptContext context) throws Throwable {
        return innerInvocation.proceed();
    }

    default Object statementHandlerQuery(InnerInvocation innerInvocation, InterceptContext context) throws Throwable {
        return innerInvocation.proceed();
    }

    default InterceptorMethodProcessor plugin(InterceptorMethodProcessor target) {
        return InnerPlugin.wrap(target, this);
    }
}
