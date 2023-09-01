package com.github.developframework.mybatis.extension.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qiushui on 2023-08-30.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

    /**
     * 表名
     */
    String value();

    /**
     * insert时是否添加ignore
     */
    boolean insertIgnore() default false;

    Column[] columns() default {};

    /**
     * 索引
     */
    Index[] indexes() default {};

    /**
     * 表格注释
     */
    String comment() default "";
}
