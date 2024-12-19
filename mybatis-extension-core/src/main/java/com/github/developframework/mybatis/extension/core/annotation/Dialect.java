package com.github.developframework.mybatis.extension.core.annotation;

import com.github.developframework.mybatis.extension.core.dialect.MybatisExtensionDialect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qiushui on 2024-12-19.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Dialect {

    Class<? extends MybatisExtensionDialect> value();
}
