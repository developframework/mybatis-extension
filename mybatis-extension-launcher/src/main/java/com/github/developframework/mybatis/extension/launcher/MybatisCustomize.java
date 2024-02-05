package com.github.developframework.mybatis.extension.launcher;

import com.github.developframework.mybatis.extension.core.autoinject.AutoInjectProvider;
import com.github.developframework.mybatis.extension.core.idgenerator.IdGenerator;
import org.apache.ibatis.session.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * @author qiushui on 2023-08-30.
 */
@FunctionalInterface
public interface MybatisCustomize {

    /**
     * 额外处理配置
     */
    void handleConfiguration(Configuration configuration);

    /**
     * 开启DDL
     */
    default boolean enableDDL() {
        return false;
    }

    /**
     * 自定义自动注入提供器
     */
    default List<? extends AutoInjectProvider> customAutoInjectProviders() {
        return Collections.emptyList();
    }

    /**
     * 自定义ID生成器
     */
    default List<? extends IdGenerator> customIdGenerators() {
        return Collections.emptyList();
    }
}
