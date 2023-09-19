package com.github.developframework.mybatis.extension.core.structs;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author qiushui on 2023-09-19.
 */
@Getter
public class Page<T> extends ArrayList<T> {

    private final Pager pager;

    private final long recordTotal;

    private final int pageTotal;

    public Page(Pager pager, Collection<? extends T> collection, long recordTotal) {
        super(collection);
        this.pager = pager;
        this.recordTotal = recordTotal;
        pageTotal = (int) (recordTotal % pager.getSize() == 0 ? recordTotal / pager.getSize() : (recordTotal / pager.getSize() + 1L));
    }

    /**
     * 空分页结果
     */
    public static <T> Page<T> empty(Pager pager) {
        return new Page<>(pager, new ArrayList<>(), 0);
    }
}
