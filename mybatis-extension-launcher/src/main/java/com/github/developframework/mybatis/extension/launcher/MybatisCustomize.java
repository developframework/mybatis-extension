package com.github.developframework.mybatis.extension.launcher;

import org.apache.ibatis.session.Configuration;

/**
 * @author qiushui on 2023-08-30.
 */
@FunctionalInterface
public interface MybatisCustomize {

    void handleConfiguration(Configuration configuration);

    default boolean enableDDL() {
        return false;
    }
}
