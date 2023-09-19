package test;

import com.github.developframework.mybatis.extension.core.autoinject.AutoInjectProvider;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import org.apache.ibatis.mapping.SqlCommandType;

import java.lang.reflect.Type;

/**
 * @author qiushui on 2023-09-18.
 */
public class DomainIdAutoInjectProvider implements AutoInjectProvider {
    @Override
    public SqlCommandType[] needInject() {
        return new SqlCommandType[]{SqlCommandType.INSERT, SqlCommandType.SELECT};
    }

    @Override
    public Object provide(EntityDefinition entityDefinition, Type fieldType) {
        return 1;
    }
}
