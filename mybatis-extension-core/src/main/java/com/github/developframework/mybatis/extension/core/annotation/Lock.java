package com.github.developframework.mybatis.extension.core.annotation;

import com.github.developframework.mybatis.extension.core.structs.LockType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qiushui on 2023-09-01.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Lock {

    LockType value() default LockType.WRITE;
}
