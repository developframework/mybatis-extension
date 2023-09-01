package com.github.developframework.mybatis.extension.core.structs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author qiushui on 2023-09-01.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum LockType {

    WRITE(" FOR UPDATE"),

    READ(" LOCK IN SHARE MODE");

    @Getter
    private final String sql;
}
