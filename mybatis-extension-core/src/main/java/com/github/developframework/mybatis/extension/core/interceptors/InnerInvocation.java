package com.github.developframework.mybatis.extension.core.interceptors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.plugin.Invocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author qiushui on 2023-09-18.
 */
@Getter
@RequiredArgsConstructor
public class InnerInvocation {

    private final Invocation invocation;

    private final InterceptorMethodProcessor target;

    private final Method method;

    private final Object[] args;

    public Object proceed() throws InvocationTargetException, IllegalAccessException {
        return method.invoke(target, args);
    }
}
