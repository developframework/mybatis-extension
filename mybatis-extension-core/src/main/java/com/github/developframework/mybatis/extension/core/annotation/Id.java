package com.github.developframework.mybatis.extension.core.annotation;

import com.github.developframework.mybatis.extension.core.idgenerator.AutoIncrementIdGenerator;
import com.github.developframework.mybatis.extension.core.idgenerator.IdGenerator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qiushui on 2023-08-30.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {

    /**
     * ID生成器
     */
    Class<? extends IdGenerator> idGenerator() default AutoIncrementIdGenerator.class;

    /**
     * 开启自增ID回填
     */
    boolean useGeneratedKey() default true;
}
