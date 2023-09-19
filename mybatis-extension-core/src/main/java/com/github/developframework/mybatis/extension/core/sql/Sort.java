package com.github.developframework.mybatis.extension.core.sql;

import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * @author qiushui on 2023-09-19.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Sort implements SqlSortPart {

    private final String property;

    private final Direction direction;

    @Override
    public String toSql(EntityDefinition entityDefinition) {
        final ColumnDefinition columnDefinition = entityDefinition.getColumnDefinition(property);
        return columnDefinition.wrapColumn() + " " + direction.name();
    }

    public enum Direction {
        ASC,
        DESC
    }

    public static Sort asc(String property) {
        return new Sort(property, Direction.ASC);
    }

    public static Sort desc(String property) {
        return new Sort(property, Direction.DESC);
    }

    public static SqlSortPart by(Sort... sorts) {
        return new MixedSort(sorts);
    }
}
