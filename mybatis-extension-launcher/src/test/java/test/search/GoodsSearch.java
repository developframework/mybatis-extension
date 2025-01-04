package test.search;

import com.github.developframework.mybatis.extension.core.sql.SqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaAssembler;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaBuilder;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlRoot;
import lombok.Getter;
import lombok.Setter;
import test.entity.Goods;

/**
 * @author qiushui on 2024-06-25.
 */
@Getter
@Setter
public class GoodsSearch implements SqlCriteriaAssembler {

    private String goodsName;

    @Override
    public SqlCriteria assemble(SqlRoot root, SqlCriteriaBuilder builder) {
        return builder.eq(root.get(Goods.Fields.goodsName), "");
    }
}