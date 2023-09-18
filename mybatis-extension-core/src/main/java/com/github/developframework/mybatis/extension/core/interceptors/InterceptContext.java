package com.github.developframework.mybatis.extension.core.interceptors;

import com.github.developframework.mybatis.extension.core.autoinject.AutoInjectProviderRegistry;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import lombok.Getter;
import lombok.Setter;

/**
 * @author qiushui on 2023-09-18.
 */
@Getter
@Setter
public class InterceptContext {

    private EntityDefinition entityDefinition;

    private AutoInjectProviderRegistry autoInjectProviderRegistry;
}
