package com.github.developframework.mybatis.extension.core.interceptors;

import com.github.developframework.mybatis.extension.core.autoinject.AutoInjectProviderRegistry;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MappedStatementMetadata;
import lombok.Getter;
import lombok.Setter;

/**
 * @author qiushui on 2023-09-18.
 */
@Getter
@Setter
public class InterceptContext {

    private MappedStatementMetadata mappedStatementMetadata;

    private EntityDefinition entityDefinition;

    private AutoInjectProviderRegistry autoInjectProviderRegistry;
}
