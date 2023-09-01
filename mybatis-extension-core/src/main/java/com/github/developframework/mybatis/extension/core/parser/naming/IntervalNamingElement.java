package com.github.developframework.mybatis.extension.core.parser.naming;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author qiushui on 2023-09-01.
 */
@Getter
@RequiredArgsConstructor
public class IntervalNamingElement implements NamingElement {

    private final Interval interval;
}
