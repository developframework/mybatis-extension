package test.mapper;

import com.github.developframework.mybatis.extension.core.BaseMapper;
import test.entity.Goods;

import java.util.List;

/**
 * @author qiushui on 2023-08-30.
 */
public interface GoodsMapper extends BaseMapper<Goods, Integer> {

    List<Goods> selectByGoodsName(String name);

}
