package test.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @author qiushui on 2023-11-24.
 */
@Getter
@Setter
public class GoodsSpec {

    private String color;

    private int size;

    public static GoodsSpec of(String str) {
        final String[] split = str.split(":");
        GoodsSpec goodsSpec = new GoodsSpec();
        goodsSpec.setColor(split[0]);
        goodsSpec.setSize(Integer.parseInt(split[1]));
        return goodsSpec;
    }

    @Override
    public String toString() {
        return color + ":" + size;
    }
}
