package com.github.developframework.mybatis.extension.core.parser.naming;

import com.github.developframework.mybatis.extension.core.annotation.SqlCustomized;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author qiushui on 2023-09-01.
 */
@Getter
@RequiredArgsConstructor
public class NamingMethodParameter {

    private final String key;

    private final SqlCustomized sqlCustomized;
}
