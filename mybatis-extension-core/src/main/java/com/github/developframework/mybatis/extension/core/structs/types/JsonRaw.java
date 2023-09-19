package com.github.developframework.mybatis.extension.core.structs.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author qiushui on 2023-09-19.
 */
@Getter
@RequiredArgsConstructor
public class JsonRaw<T> {

    private final T value;
}
