package com.github.developframework.mybatis.boot;

import com.github.developframework.mybatis.extension.core.autoinject.*;
import com.github.developframework.mybatis.extension.core.dialect.MybatisExtensionDialect;
import com.github.developframework.mybatis.extension.core.idgenerator.AutoIncrementIdGenerator;
import com.github.developframework.mybatis.extension.core.idgenerator.IdGenerator;
import com.github.developframework.mybatis.extension.core.idgenerator.IdGeneratorRegistry;
import com.github.developframework.mybatis.extension.dialect.mysql.MysqlMybatisExtensionDialect;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author qiushui on 2023-10-07.
 */
@Configuration
@ComponentScan("com.github.developframework.mybatis.boot")
public class MybatisExtensionAutoConfiguration {

    @Bean
    public AutoInjectProviderRegistry autoInjectProviderRegistry(ObjectProvider<AutoInjectProvider> autoInjectProviders) {
        final AutoInjectProviderRegistry registry = new AutoInjectProviderRegistry();
        autoInjectProviders.forEach(provider -> registry.put(provider.getClass(), provider));
        return registry;
    }

    @Bean
    public IdGeneratorAutoInjectProvider idGeneratorAutoInjectProvider(ObjectProvider<IdGenerator> idGenerators) {
        IdGeneratorRegistry idGeneratorRegistry = new IdGeneratorRegistry();
        idGeneratorRegistry.register(new AutoIncrementIdGenerator());
        for (IdGenerator idGenerator : idGenerators) {
            idGeneratorRegistry.register(idGenerator);
        }
        return new IdGeneratorAutoInjectProvider(idGeneratorRegistry);
    }

    @Bean
    public AuditCreateTimeAutoInjectProvider auditCreateDateTimeAutoInjectProvider() {
        return new AuditCreateTimeAutoInjectProvider();
    }

    @Bean
    public AuditModifyTimeAutoInjectProvider auditModifyTimeAutoInjectProvider() {
        return new AuditModifyTimeAutoInjectProvider();
    }

    @ConditionalOnClass(name = "com.mysql.cj.jdbc.Driver")
    @ConditionalOnMissingBean
    @Bean
    public MybatisExtensionDialect defaultMybatisExtensionDialect() {
        return new MysqlMybatisExtensionDialect();
    }
}
