package com.github.developframework.mybatis.boot;

import com.github.developframework.mybatis.extension.core.autoinject.AutoInjectProviderRegistry;
import com.github.developframework.mybatis.extension.core.dialect.MybatisExtensionDialect;
import com.github.developframework.mybatis.extension.core.interceptors.MybatisExtensionInterceptor;
import com.github.developframework.mybatis.extension.launcher.MybatisExtensionLauncher;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author qiushui on 2023-10-07.
 */
@Component
public class ExtensionMybatisListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        ObjectProvider<SqlSessionFactory> sqlSessionFactoryObjectProvider = applicationContext.getBeanProvider(SqlSessionFactory.class);
        MybatisExtensionInterceptor mybatisExtensionInterceptor = applicationContext.getBean(MybatisExtensionInterceptor.class);
        AutoInjectProviderRegistry autoInjectProviderRegistry = applicationContext.getBean(AutoInjectProviderRegistry.class);
        MybatisExtensionProperties mybatisExtensionProperties = applicationContext.getBean(MybatisExtensionProperties.class);
        MybatisExtensionDialect defaultMybatisExtensionDialect = applicationContext.getBean("defaultMybatisExtensionDialect", MybatisExtensionDialect.class);

        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryObjectProvider) {
            MybatisExtensionLauncher.configureSqlSessionFactory(
                    sqlSessionFactory,
                    defaultMybatisExtensionDialect,
                    mybatisExtensionInterceptor,
                    autoInjectProviderRegistry,
                    mybatisExtensionProperties.isEnableDDL()
            );
        }
    }
}
