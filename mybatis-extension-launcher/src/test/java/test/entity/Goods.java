package test.entity;

import com.github.developframework.mybatis.extension.core.annotation.Id;
import com.github.developframework.mybatis.extension.core.annotation.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author qiushui on 2023-08-30.
 */
@Getter
@Setter
@Table("goods")
public class Goods {

    @Id
    private Integer id;

    private String goodsName;

    private String quantity;

    private LocalDateTime createTime;

}
