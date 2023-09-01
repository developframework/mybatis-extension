package com.github.developframework.mybatis.extension.core;

import com.github.developframework.mybatis.extension.core.structs.MappedStatementMetadata;
import lombok.Getter;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author qiushui on 2023-08-30.
 */
public class MappedStatementMetadataManager {

    final Map<String, Map<String, MappedStatementMetadata>> mappedStatementMap;

    @Getter
    final Map<String, MappedStatementMetadata> metadataMap;

    public MappedStatementMetadataManager(Configuration configuration) {
        mappedStatementMap = new HashMap<>();
        metadataMap = new HashMap<>();
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

            if (obj instanceof MappedStatement ms) {
                MappedStatementMetadata metadata = MappedStatementMetadata.parse(ms);
                metadataMap.put(ms.getId(), metadata);
                mappedStatementMap
                        .computeIfAbsent(metadata.getMapperClass().getName(), k -> new HashMap<>())
                        .put(metadata.getMapperMethod().getName(), metadata);
            }
        }
    }

    public Optional<MappedStatementMetadata> get(String interfaceName, String methodName) {
        return Optional
                .ofNullable(mappedStatementMap.get(interfaceName))
                .map(m -> m.get(methodName));
    }
}
