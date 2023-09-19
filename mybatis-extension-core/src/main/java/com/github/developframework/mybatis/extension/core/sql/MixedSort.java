package com.github.developframework.mybatis.extension.core.sql;

import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author qiushui on 2023-09-19.
 */
@RequiredArgsConstructor
public class MixedSort implements SqlSortPart {

    private final Sort[] sortArray;

    @Override
    public String toSql(EntityDefinition entityDefinition) {
        return Arrays.stream(sortArray)
                .map(s -> s.toSql(entityDefinition))
                .collect(Collectors.joining(", ", " ORDER BY ", ""));
    }
}
