package com.github.developframework.mybatis.extension.core;

import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MappedStatementMetadata;
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
            MappedStatementMetadataManager mappedStatementMetadataManager = new MappedStatementMetadataManager(configuration);
            Map<String, MappedStatementMetadata> metadataMap = mappedStatementMetadataManager.getMetadataMap();
            this.metadataMap.putAll(metadataMap);

            for (MappedStatementMetadata metadata : metadataMap.values()) {
                Class<?> entityClass = metadata.getEntityClass();
                if (entityClass == null) {
                    continue;
                }
                EntityDefinition entityDefinition = new EntityDefinition(entityClass);
                entityDefinitionRegistry.put(entityClass, entityDefinition);

                MapperNamingBuilder mapperNamingBuilder = new MapperNamingBuilder(configuration, metadata.getMapperClass(), entityDefinition);
                mapperNamingBuilder.parse();

            }
        }
    }
}
