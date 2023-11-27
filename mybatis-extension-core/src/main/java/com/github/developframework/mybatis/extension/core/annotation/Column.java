package com.github.developframework.mybatis.extension.core.annotation;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qiushui on 2023-08-30.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    /**
     * 字段名称
     */
    String name() default "";

    /**
     * 自定义类型申明
     */
    String customizeType() default "";

    /**
     * 是否可以Null值
     */
    boolean nullable() default false;

    /**
     * 长度
     */
    int length() default 0;

    /**
     * 精度
     */
    int scale() default 0;

    /**
     * 是否无符号
     */
    boolean unsigned() default true;

    /**
     * 默认值
     */
    String defaultValue() default "";

    /**
     * mybatis决定typeHandler的javaType
     */
    Class<?> javaType() default void.class;

    /**
     * mybatis决定typeHandler的jdbcType
     */
    JdbcType jdbcType() default JdbcType.UNDEFINED;

    /**
     * 字段注释
     */
    String comment() default "";

    /**
     * 指定typeHandler
     */
    Class<? extends TypeHandler<?>> typeHandler() default UnknownTypeHandler.class;
}
