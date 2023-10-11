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

    void handleConfiguration(Configuration configuration);

    default boolean enableDDL() {
        return false;
    }

    default List<? extends AutoInjectProvider> customAutoInjectProviders() {
        return Collections.emptyList();
    }

    default List<? extends IdGenerator> customIdGenerators() {
        return Collections.emptyList();
    }
}
