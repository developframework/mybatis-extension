package com.github.developframework.mybatis.extension.core.structs;

import lombok.Getter;
import lombok.Setter;

/**
 * @author qiushui on 2023-09-19.
 */
@Getter
public class Pager {

    public static final int DEFAULT_FIRST_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 1000;

    private final int page;

    private final int size;

    @Setter
    private Long total;

    public Pager() {
        this.page = DEFAULT_FIRST_PAGE;
        this.size = DEFAULT_SIZE;
    }

    public Pager(int page, int size) {
        this.page = Math.max(0, page);
        if (size < 0) {
            this.size = DEFAULT_SIZE;
        } else {
            this.size = Math.min(size, MAX_SIZE);
        }
    }

    public int getOffset() {
        return size * page;
    }
}
