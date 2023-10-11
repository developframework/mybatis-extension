package com.github.developframework.mybatis.extension.core.autoinject;

import com.github.developframework.mybatis.extension.core.idgenerator.IdGenerator;
import com.github.developframework.mybatis.extension.core.idgenerator.IdGeneratorRegistry;
import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.mapping.SqlCommandType;

/**
 * @author qiushui on 2023-10-11.
 */
@RequiredArgsConstructor
public class IdGeneratorAutoInjectProvider implements AutoInjectProvider {

    private final IdGeneratorRegistry idGeneratorRegistry;

    @Override
    public SqlCommandType[] needInject() {
        return new SqlCommandType[]{SqlCommandType.INSERT};
    }

    @Override
    public Object provide(EntityDefinition entityDefinition, ColumnDefinition columnDefinition, Object entity) {
        final Class<? extends IdGenerator> idGeneratorClass = columnDefinition.getIdGeneratorClass();
        if (idGeneratorClass != null) {
            final IdGenerator idGenerator = idGeneratorRegistry.get(columnDefinition.getIdGeneratorClass());
            return idGenerator.generate(entity);
        } else {
            return null;
        }
    }
}
