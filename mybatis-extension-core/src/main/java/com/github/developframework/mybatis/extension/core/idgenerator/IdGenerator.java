package com.github.developframework.mybatis.extension.core.idgenerator;

/**
 * @author qiushui on 2023-10-11.
 */
public interface IdGenerator {

    Object generate(Object entity);
}
