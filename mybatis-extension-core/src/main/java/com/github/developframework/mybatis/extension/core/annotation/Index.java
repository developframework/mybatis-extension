package com.github.developframework.mybatis.extension.core.annotation;

import com.github.developframework.mybatis.extension.core.structs.IndexMode;
import com.github.developframework.mybatis.extension.core.structs.IndexType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qiushui on 2023-08-30.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Index {

    /**
     * 索引名称
     */
    String name() default "";

    /**
     * 索引类型
     */
    IndexType type();

    /**
     * 属性
     */
    String[] properties();

    /**
     * 索引实现方式
     */
    IndexMode mode() default IndexMode.BTREE;
}
