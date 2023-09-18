package com.github.developframework.mybatis.extension.core.autoinject;

import java.util.HashMap;

/**
 * @author qiushui on 2023-09-18.
 */
public class AutoInjectProviderRegistry extends HashMap<Class<? extends AutoInjectProvider>, AutoInjectProvider> {

    public AutoInjectProvider getAutoInjectProvider(Class<? extends AutoInjectProvider> autoInjectProviderClass) {
        final AutoInjectProvider autoInjectProvider = get(autoInjectProviderClass);
        if (autoInjectProvider != null) {
            return autoInjectProvider;
        } else {
            throw new IllegalStateException("未注册" + autoInjectProviderClass.getName() + "实例");
        }
    }
}
