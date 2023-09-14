package com.github.developframework.mybatis.extension.core;

import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MappedStatementMetadata;
import com.github.developframework.mybatis.extension.core.utils.MybatisUtils;
import lombok.Getter;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * mybatis扩展核心
 *
 * @author qiushui on 2023-08-30.
 */
@Getter
public class MybatisExtensionCore {

    private final EntityDefinitionRegistry entityDefinitionRegistry = new EntityDefinitionRegistry();

    private final Map<String, MappedStatementMetadata> metadataMap = new HashMap<>();

    public MybatisExtensionCore(SqlSessionFactory... sqlSessionFactories) {
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactories) {
            Configuration configuration = sqlSessionFactory.getConfiguration();
            for (Class<?> mapperClass : configuration.getMapperRegistry().getMappers()) {
                Class<?> entityClass = MybatisUtils.getEntityClass(mapperClass);
                EntityDefinition entityDefinition = entityDefinitionRegistry.register(entityClass);
                MapperNamingBuilder mapperNamingBuilder = new MapperNamingBuilder(configuration, mapperClass, entityDefinition);
                mapperNamingBuilder.parse();
            }
        }
    }
}
