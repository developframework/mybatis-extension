package com.github.developframework.mybatis.extension.core.structs;

import com.github.developframework.mybatis.extension.core.utils.NameUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author qiushui on 2023-09-01.
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class IndexCompare {

    private final String name;

    private final String[] columns;

    private final IndexMode mode;

    private final IndexType type;

    @Override
    public String toString() {
        String columnStr = NameUtils.wrap(name) + Arrays.stream(columns).map(NameUtils::wrap).collect(Collectors.joining(",", "(", ")"));
        switch (type) {
            case NORMAL:
                return "INDEX " + columnStr + " USING " + mode.name();
            case UNIQUE:
                return "UNIQUE INDEX " + columnStr + " USING " + mode.name();
            case FULLTEXT:
                return "FULLTEXT INDEX " + columnStr;
            case SPATIAL:
                return "SPATIAL INDEX " + columnStr;
            default:
                throw new AssertionError();
        }
    }
}
