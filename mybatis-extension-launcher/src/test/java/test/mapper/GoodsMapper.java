package test.mapper;

import com.github.developframework.mybatis.extension.core.BaseMapper;
import com.github.developframework.mybatis.extension.core.structs.Page;
import com.github.developframework.mybatis.extension.core.structs.Pager;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import test.entity.Goods;
import test.search.GoodsSearch;

import java.util.List;

/**
 * @author qiushui on 2023-08-30.
 */
public interface GoodsMapper extends BaseMapper<Goods, Integer> {

    List<Goods> selectByGoodsName(String name);

    List<Goods> selectByGoodsNameAndQuantity(String name, int quantity);

    List<Goods> selectByQuantityTrue();

    void updateGoodsNameQuantity(@Param("goodsName") String goodsName, @Param("quantity") int quantity, @Param("id") int id);

    @Select("SELECT * FROM goods WHERE goods_name = #{search.goodsName}")
    Page<Goods> selectCustomPager(Pager pager, @Param("search") GoodsSearch search);

}
