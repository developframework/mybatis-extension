package com.github.developframework.mybatis.extension.core.interceptors;

import lombok.RequiredArgsConstructor;
import org.apache.ibatis.plugin.Invocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author qiushui on 2023-09-18.
 */
@RequiredArgsConstructor
public class InnerPlugin implements InvocationHandler {

    private final InterceptorMethodProcessor target;

    private final InnerInterceptor innerInterceptor;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final InnerInvocation innerInvocation = new InnerInvocation((Invocation) args[0], target, method, args);
        final Method interceptorMethod = innerInterceptor.getClass().getMethod(method.getName(), InnerInvocation.class, InterceptContext.class);
        return interceptorMethod.invoke(innerInterceptor, innerInvocation, args[1]);
    }

    public static InterceptorMethodProcessor wrap(InterceptorMethodProcessor target, InnerInterceptor innerInterceptor) {
        return (InterceptorMethodProcessor) Proxy.newProxyInstance(
                InnerPlugin.class.getClassLoader(),
                new Class[]{InterceptorMethodProcessor.class},
                new InnerPlugin(target, innerInterceptor)
        );
    }
}
