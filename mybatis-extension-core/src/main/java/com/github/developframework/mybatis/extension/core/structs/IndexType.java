package com.github.developframework.mybatis.extension.core.structs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author qiushui on 2023-08-30.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum IndexType {

    UNIQUE("UK"),

    NORMAL("K"),

    FULLTEXT("F"),

    SPATIAL("S");

    private final String prefix;
}
