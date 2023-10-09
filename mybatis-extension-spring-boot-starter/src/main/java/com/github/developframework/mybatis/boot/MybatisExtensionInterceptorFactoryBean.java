package com.github.developframework.mybatis.boot;

import com.github.developframework.mybatis.extension.core.interceptors.MybatisExtensionInterceptor;
import com.github.developframework.mybatis.extension.core.interceptors.inner.*;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author qiushui on 2023-10-07.
 */
@Component
public class MybatisExtensionInterceptorFactoryBean implements FactoryBean<MybatisExtensionInterceptor> {

    @Override
    public Class<?> getObjectType() {
        return MybatisExtensionInterceptor.class;
    }

    @Override
    public MybatisExtensionInterceptor getObject() throws Exception {
        return new MybatisExtensionInterceptor(
                List.of(
                        new OptimisticLockInnerInterceptor(),
                        new CompositeIdInnerInterceptor(),
                        new PagingInnerInterceptor(),
                        new SqlCriteriaAssemblerInnerInterceptor(),
                        new AutoInjectInnerInterceptor()
                )
        );
    }
}
