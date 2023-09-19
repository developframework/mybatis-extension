package com.github.developframework.mybatis.extension.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qiushui on 2023-09-19.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CountStatement {

    /**
     * 查询数量SQL的statement id
     */
    String value();
}
