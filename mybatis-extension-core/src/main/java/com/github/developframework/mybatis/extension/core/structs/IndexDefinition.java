package com.github.developframework.mybatis.extension.core.structs;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

/**
 * @author qiushui on 2023-08-31.
 */
@Getter
@Setter
public class IndexDefinition {

    private String name;

    private IndexType type;

    private IndexMode mode;

    private ColumnDefinition[] columnDefinitions;

    public IndexCompare toIndexCompare() {
        final String[] columns = Arrays.stream(columnDefinitions)
                .map(ColumnDefinition::getColumn)
                .toArray(String[]::new);
        return new IndexCompare(
                name.isEmpty() ? (type.getPrefix() + String.join("_", columns)) : name,
                columns,
                mode,
                type
        );
    }
}
