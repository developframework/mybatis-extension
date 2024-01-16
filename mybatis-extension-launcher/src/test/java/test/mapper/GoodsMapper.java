package test.mapper;

import com.github.developframework.mybatis.extension.core.BaseMapper;
import org.apache.ibatis.annotations.Param;
import test.entity.Goods;

import java.util.List;

/**
 * @author qiushui on 2023-08-30.
 */
public interface GoodsMapper extends BaseMapper<Goods, Integer> {

    List<Goods> selectByGoodsName(String name);

    void updateGoodsNameQuantity(@Param("goodsName") String goodsName, @Param("quantity") int quantity, @Param("id") int id);

}
