package test;

import com.github.developframework.mybatis.extension.core.autoinject.AutoInjectProvider;
import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import org.apache.ibatis.mapping.SqlCommandType;

/**
 * @author qiushui on 2023-09-18.
 */
public class DomainIdAutoInjectProvider implements AutoInjectProvider {
    @Override
    public SqlCommandType[] needInject() {
        return new SqlCommandType[]{SqlCommandType.INSERT, SqlCommandType.SELECT};
    }

    @Override
    public Object provide(EntityDefinition entityDefinition, ColumnDefinition columnDefinition, Object entity) {
        return null;
    }
}
