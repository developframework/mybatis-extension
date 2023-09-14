package test.entity;

import com.github.developframework.mybatis.extension.core.annotation.Id;
import com.github.developframework.mybatis.extension.core.annotation.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @author qiushui on 2023-08-30.
 */
@Getter
@Setter
@Table("goods")
@ToString
@NoArgsConstructor
public class Goods {

    @Id
    private Integer id;

    private String goodsName;

    private Integer quantity;

    private LocalDateTime createTime;

    public Goods(String goodsName, Integer quantity, LocalDateTime createTime) {
        this.goodsName = goodsName;
        this.quantity = quantity;
        this.createTime = createTime;
    }
}
