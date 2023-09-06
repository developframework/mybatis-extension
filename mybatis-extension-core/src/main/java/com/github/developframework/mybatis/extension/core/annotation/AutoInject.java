package com.github.developframework.mybatis.extension.core.annotation;

import com.github.developframework.mybatis.extension.core.autoinject.AutoInjectProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qiushui on 2023-09-05.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoInject {

    // 注入值提供器
    Class<? extends AutoInjectProvider> value();

    // 标注多租户字段
    boolean multipleTenant() default false;
}
