package com.github.developframework.mybatis.extension.launcher;

import com.github.developframework.mybatis.extension.core.DatabaseDDLExecutor;
import com.github.developframework.mybatis.extension.core.MybatisExtensionCore;
import com.github.developframework.mybatis.extension.core.autoinject.AuditCreateTimeAutoInjectProvider;
import com.github.developframework.mybatis.extension.core.autoinject.AuditModifyTimeAutoInjectProvider;
import com.github.developframework.mybatis.extension.core.autoinject.AutoInjectProvider;
import com.github.developframework.mybatis.extension.core.autoinject.AutoInjectProviderRegistry;
import com.github.developframework.mybatis.extension.core.interceptors.MybatisExtensionInterceptor;
import com.github.developframework.mybatis.extension.core.interceptors.inner.AutoInjectInnerInterceptor;
import com.github.developframework.mybatis.extension.core.interceptors.inner.SqlCriteriaAssemblerInnerInterceptor;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author qiushui on 2023-08-30.
 */
public class ExtensionMybatisLauncher {

    public static SqlSessionFactory open(DataSourceMetadata dataSourceMetadata, MybatisCustomize customize) {
        DataSource dataSource = buildDefaultDataSource(dataSourceMetadata);
        return open(dataSource, customize);
    }

    public static SqlSessionFactory open(DataSource dataSource, MybatisCustomize customize) {
        Configuration configuration = buildConfiguration(dataSource);
        configureTypeHandlers(configuration);
        configureTypeAliases(configuration);
        // 配置拦截器
        final MybatisExtensionInterceptor mybatisExtensionInterceptor = initializeInterceptor(configuration);
        // 自定义配置
        if (customize != null) {
            customize.handleConfiguration(configuration);
        }
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        MybatisExtensionCore core = new MybatisExtensionCore(sqlSessionFactory);

        // 装配自动注入提供器
        final AutoInjectProviderRegistry autoInjectProviderRegistry = assembleAutoInjectProviders(customize);

        // 给拦截器设置关联组件
        mybatisExtensionInterceptor.setAutoInjectProviderRegistry(autoInjectProviderRegistry);
        mybatisExtensionInterceptor.setEntityDefinitionRegistry(core.getEntityDefinitionRegistry());
        mybatisExtensionInterceptor.setMappedStatementMetadataRegistry(core.getMappedStatementMetadataRegistry());

        // 执行DDL
        if (customize != null && customize.enableDDL()) {
            DatabaseDDLExecutor databaseDDLExecutor = new DatabaseDDLExecutor(sqlSessionFactory, core.getEntityDefinitionRegistry());
            databaseDDLExecutor.executeDDL();
        }
        return sqlSessionFactory;
    }

    private static DataSource buildDefaultDataSource(DataSourceMetadata dataSourceMetadata) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(dataSourceMetadata.getDriverClass());
        hikariConfig.setJdbcUrl(dataSourceMetadata.getJdbcUrl());
        hikariConfig.setUsername(dataSourceMetadata.getUsername());
        hikariConfig.setPassword(dataSourceMetadata.getPassword());
        return new HikariDataSource(hikariConfig);
    }

    private static Configuration buildConfiguration(DataSource dataSource) {
        Configuration configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setUseGeneratedKeys(true);
        configuration.setLogPrefix("mybatis.");
        configuration.setEnvironment(new Environment("mysql", new JdbcTransactionFactory(), dataSource));
        return configuration;
    }

    private static void configureTypeHandlers(Configuration configuration) {
        final TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
//        typeHandlerRegistry.register(StringArrayTypeHandler.class);
//        typeHandlerRegistry.register(StringListTypeHandler.class);
//        typeHandlerRegistry.register(StringSetTypeHandler.class);
    }

    private static void configureTypeAliases(Configuration configuration) {
        final TypeAliasRegistry typeAliasRegistry = configuration.getTypeAliasRegistry();
    }

    private static MybatisExtensionInterceptor initializeInterceptor(Configuration configuration) {
        MybatisExtensionInterceptor mybatisExtensionInterceptor = new MybatisExtensionInterceptor(
                List.of(
//                        new OptimisticLockInnerInterceptor(),
//                        new CompositeIdInnerInterceptor(),
//                        new PagingInnerInterceptor(),
                        new SqlCriteriaAssemblerInnerInterceptor(),
                        new AutoInjectInnerInterceptor()
                )
        );
        configuration.addInterceptor(mybatisExtensionInterceptor);
        return mybatisExtensionInterceptor;
    }

    private static AutoInjectProviderRegistry assembleAutoInjectProviders(MybatisCustomize config) {
        AutoInjectProviderRegistry autoInjectProviderRegistry = new AutoInjectProviderRegistry();
        autoInjectProviderRegistry.put(AuditCreateTimeAutoInjectProvider.class, new AuditCreateTimeAutoInjectProvider());
        autoInjectProviderRegistry.put(AuditModifyTimeAutoInjectProvider.class, new AuditModifyTimeAutoInjectProvider());
        if (config != null) {
            for (AutoInjectProvider provider : config.customAutoInjectProviders()) {
                autoInjectProviderRegistry.put(provider.getClass(), provider);
            }
        }
        return autoInjectProviderRegistry;
    }
}
