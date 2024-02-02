package com.github.developframework.mybatis.extension.launcher;

import com.github.developframework.mybatis.extension.core.DatabaseDDLExecutor;
import com.github.developframework.mybatis.extension.core.MybatisExtensionCore;
import com.github.developframework.mybatis.extension.core.autoinject.*;
import com.github.developframework.mybatis.extension.core.idgenerator.AutoIncrementIdGenerator;
import com.github.developframework.mybatis.extension.core.idgenerator.IdGenerator;
import com.github.developframework.mybatis.extension.core.idgenerator.IdGeneratorRegistry;
import com.github.developframework.mybatis.extension.core.interceptors.MybatisExtensionInterceptor;
import com.github.developframework.mybatis.extension.core.interceptors.inner.*;
import com.github.developframework.mybatis.extension.core.typehandlers.StringArrayTypeHandler;
import com.github.developframework.mybatis.extension.core.typehandlers.StringListTypeHandler;
import com.github.developframework.mybatis.extension.core.typehandlers.StringSetTypeHandler;
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
public class MybatisExtensionLauncher {

    public static SqlSessionFactory open(DataSourceMetadata dataSourceMetadata, MybatisCustomize customize) {
        DataSource dataSource = buildDefaultDataSource(dataSourceMetadata);
        return open(dataSource, customize);
    }

    public static SqlSessionFactory open(DataSource dataSource, MybatisCustomize customize) {
        Configuration configuration = buildConfiguration(dataSource);
        // 配置类型转换器
        configureTypeHandlers(configuration);
        // 配置类型别名
        configureTypeAliases(configuration);
        // 配置拦截器
        final MybatisExtensionInterceptor mybatisExtensionInterceptor = initializeInterceptor(configuration);
        // ID生成器
        final IdGeneratorRegistry idGeneratorRegistry = assembleIdGenerators(customize);
        // 装配自动注入提供器
        final AutoInjectProviderRegistry autoInjectProviderRegistry = assembleAutoInjectProviders(customize, idGeneratorRegistry);

        // 自定义配置
        if (customize != null) {
            customize.handleConfiguration(configuration);
        }
        // 构建SqlSessionFactory
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

        // 配置SqlSessionFactory
        configureSqlSessionFactory(
                sqlSessionFactory,
                mybatisExtensionInterceptor,
                autoInjectProviderRegistry,
                customize != null && customize.enableDDL()
        );

        return sqlSessionFactory;
    }

    public static void configureSqlSessionFactory(
            SqlSessionFactory sqlSessionFactory,
            MybatisExtensionInterceptor mybatisExtensionInterceptor,
            AutoInjectProviderRegistry autoInjectProviderRegistry,
            boolean enableDDL
    ) {
        // 扩展核心启动
        MybatisExtensionCore core = new MybatisExtensionCore(sqlSessionFactory);

        // 给拦截器设置关联组件
        mybatisExtensionInterceptor.setAutoInjectProviderRegistry(autoInjectProviderRegistry);
        mybatisExtensionInterceptor.setEntityDefinitionRegistry(core.getEntityDefinitionRegistry());
        mybatisExtensionInterceptor.setMappedStatementMetadataRegistry(core.getMappedStatementMetadataRegistry());

        // 执行DDL
        if (enableDDL) {
            DatabaseDDLExecutor databaseDDLExecutor = new DatabaseDDLExecutor(sqlSessionFactory, core.getEntityDefinitionRegistry());
            databaseDDLExecutor.executeDDL();
        }
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
        typeHandlerRegistry.register(StringArrayTypeHandler.class);
        typeHandlerRegistry.register(StringListTypeHandler.class);
        typeHandlerRegistry.register(StringSetTypeHandler.class);
    }

    private static void configureTypeAliases(Configuration configuration) {
        final TypeAliasRegistry typeAliasRegistry = configuration.getTypeAliasRegistry();
    }

    private static MybatisExtensionInterceptor initializeInterceptor(Configuration configuration) {
        MybatisExtensionInterceptor mybatisExtensionInterceptor = new MybatisExtensionInterceptor(
                List.of(
                        new OptimisticLockInnerInterceptor(),
                        new CompositeIdInnerInterceptor(),
                        new PagingInnerInterceptor(),
                        new SqlCriteriaAssemblerInnerInterceptor(),
                        new AutoInjectInnerInterceptor()
                )
        );
        configuration.addInterceptor(mybatisExtensionInterceptor);
        return mybatisExtensionInterceptor;
    }

    private static AutoInjectProviderRegistry assembleAutoInjectProviders(MybatisCustomize customize, IdGeneratorRegistry idGeneratorRegistry) {
        AutoInjectProviderRegistry autoInjectProviderRegistry = new AutoInjectProviderRegistry();
        autoInjectProviderRegistry.put(AuditCreateTimeAutoInjectProvider.class, new AuditCreateTimeAutoInjectProvider());
        autoInjectProviderRegistry.put(AuditModifyTimeAutoInjectProvider.class, new AuditModifyTimeAutoInjectProvider());
        autoInjectProviderRegistry.put(IdGeneratorAutoInjectProvider.class, new IdGeneratorAutoInjectProvider(idGeneratorRegistry));
        if (customize != null) {
            for (AutoInjectProvider provider : customize.customAutoInjectProviders()) {
                autoInjectProviderRegistry.put(provider.getClass(), provider);
            }
        }
        return autoInjectProviderRegistry;
    }

    private static IdGeneratorRegistry assembleIdGenerators(MybatisCustomize customize) {
        IdGeneratorRegistry idGeneratorRegistry = new IdGeneratorRegistry();
        idGeneratorRegistry.register(new AutoIncrementIdGenerator());
        if (customize != null) {
            for (IdGenerator idGenerator : customize.customIdGenerators()) {
                idGeneratorRegistry.register(idGenerator);
            }
        }
        return idGeneratorRegistry;
    }
}
