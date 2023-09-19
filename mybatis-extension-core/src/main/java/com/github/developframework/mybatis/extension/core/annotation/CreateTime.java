package com.github.developframework.mybatis.extension.core.annotation;

import com.github.developframework.mybatis.extension.core.autoinject.AuditCreateTimeAutoInjectProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qiushui on 2023-09-18.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@AutoInject(AuditCreateTimeAutoInjectProvider.class)
public @interface CreateTime {
}
