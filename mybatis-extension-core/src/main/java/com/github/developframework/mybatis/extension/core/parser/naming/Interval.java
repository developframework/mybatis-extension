package com.github.developframework.mybatis.extension.core.parser.naming;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 间隔符
 *
 * @author qiushui on 2023-09-01.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Interval {

    AND(" AND "),
    OR(" OR "),
    EMPTY("");

    private final String text;
}
