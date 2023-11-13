package test.entity;

import com.github.developframework.mybatis.extension.core.annotation.Id;
import com.github.developframework.mybatis.extension.core.annotation.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

/**
 * @author qiushui on 2023-08-30.
 */
@Getter
@Setter
@FieldNameConstants
@Table("goods")
@ToString
@NoArgsConstructor
public class Goods extends AuditPO {

    @Id
    private Integer id;

    private String goodsName;

    private Integer quantity;

    //    @AutoInject(multipleTenant = true, value = DomainIdAutoInjectProvider.class)
    private Integer domainId;

    public Goods(String goodsName, Integer quantity) {
        this.goodsName = goodsName;
        this.quantity = quantity;
    }
}
