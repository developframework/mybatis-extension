package com.github.developframework.mybatis.extension.core.idgenerator;

/**
 * @author qiushui on 2023-10-11.
 */
public class NoIdGenerator implements IdGenerator {

    @Override
    public Object generate(Object entity) {
        return null;
    }
}
