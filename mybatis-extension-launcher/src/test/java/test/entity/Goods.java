package test.entity;

import com.github.developframework.mybatis.extension.core.annotation.AutoInject;
import com.github.developframework.mybatis.extension.core.annotation.Column;
import com.github.developframework.mybatis.extension.core.annotation.Id;
import com.github.developframework.mybatis.extension.core.annotation.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import test.DomainIdAutoInjectProvider;
import test.typehandler.GoodsSpecArrayTypeHandler;

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

    @AutoInject(multipleTenant = true, value = DomainIdAutoInjectProvider.class)
    private Integer domainId;

    @Column(typeHandler = GoodsSpecArrayTypeHandler.class)
    private GoodsSpec[] goodsSpecs;

    public Goods(String goodsName, Integer quantity) {
        this.goodsName = goodsName;
        this.quantity = quantity;
    }
}
