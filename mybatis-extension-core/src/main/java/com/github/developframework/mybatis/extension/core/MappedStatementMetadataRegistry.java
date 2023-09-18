package com.github.developframework.mybatis.extension.core;

import com.github.developframework.mybatis.extension.core.structs.MappedStatementMetadata;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.HashMap;
import java.util.Map;

/**
 * @author qiushui on 2023-09-18.
 */
public class MappedStatementMetadataRegistry {

    private final Map<String, MappedStatementMetadata> internalMap = new HashMap<>();

    public void register(MappedStatement mappedStatement) {
        internalMap.computeIfAbsent(mappedStatement.getId(), k -> MappedStatementMetadata.parse(mappedStatement));
    }

    public boolean exists(String mappedStatementId) {
        return internalMap.containsKey(mappedStatementId);
    }

    public MappedStatementMetadata get(String mappedStatementId) {
        MappedStatementMetadata metadata = internalMap.get(mappedStatementId);
        if (metadata == null) {
            throw new MybatisExtensionException("不存在\"" + mappedStatementId + "\"");
        }
        return metadata;
    }
}
