package com.github.developframework.mybatis.extension.core;

import com.github.developframework.mybatis.extension.core.annotation.Dialect;
import com.github.developframework.mybatis.extension.core.dialect.MybatisExtensionDialect;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.utils.MybatisUtils;
import lombok.Getter;
import org.apache.ibatis.mapping.MappedStatement;
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

    private final MappedStatementMetadataRegistry mappedStatementMetadataRegistry = new MappedStatementMetadataRegistry();

    public MybatisExtensionCore(MybatisExtensionDialect defaultDialect, SqlSessionFactory... sqlSessionFactories) {
        Map<Class<? extends MybatisExtensionDialect>, MybatisExtensionDialect> dialectMap = new HashMap<>();
        dialectMap.put(defaultDialect.getClass(), defaultDialect);
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactories) {

            Configuration configuration = sqlSessionFactory.getConfiguration();

            // 载入BaseMapper下已成为MappedStatement的方法
            for (Object obj : configuration.getMappedStatements()) {

                /**
                 * 这里需要取出真实的MappedStatement
                 * 因为configuration.mappedStatements里面其实可能包含StrictMap.Ambiguity占位类型
                 * 参考 {@link org.apache.ibatis.session.Configuration.StrictMap#put(Object, Object)}
                 * 里面塞入了两个key 一个是长名，一个是短名，Mybatis贴心地为了可以使用短名查找mappedStatement而搞的
                 * <p>
                 * sqlSession.selectOne("findById", 1);
                 * sqlSession.selectOne("com.xxx.XxxMapper.findById", 1);
                 * <p>
                 * 如果两个mapper里的方法同名的话，将会塞入一个{@link org.apache.ibatis.session.Configuration.StrictMap.Ambiguity}
                 * 作为占位，在sqlSession.selectOne去取时会报错
                 */

                if (obj instanceof MappedStatement mappedStatement) {
                    mappedStatementMetadataRegistry.register(mappedStatement);
                }
            }

            // 载入BaseMapper下未生成MappedStatement的方法
            for (Class<?> mapperClass : configuration.getMapperRegistry().getMappers()) {
                if (BaseMapper.class.isAssignableFrom(mapperClass)) {
                    Class<?> entityClass = MybatisUtils.getEntityClass(mapperClass);
                    final Dialect dialect = entityClass.getAnnotation(Dialect.class);


                    EntityDefinition entityDefinition = entityDefinitionRegistry.register(defaultDialect, entityClass);

                    MapperExtensionBuilder mapperExtensionBuilder = new MapperExtensionBuilder(
                            configuration,
                            mapperClass,
                            entityDefinition,
                            mappedStatementMetadataRegistry
                    );
                    mapperExtensionBuilder.parse();
                }
            }
        }
    }
}
