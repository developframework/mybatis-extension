package com.github.developframework.mybatis.extension.core.structs;

import com.github.developframework.mybatis.extension.core.utils.NameUtils;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;

/**
 * @author qiushui on 2023-08-30.
 */
@Getter
@Setter
public class ColumnDefinition {

    private String column;

    private String property;

    private Type propertyType;

    private ColumnBuildMetadata columnBuildMetadata;

    private ColumnMybatisPlaceholder columnMybatisPlaceholder;

    private boolean version;

    private boolean useGeneratedKey;

    private boolean primaryKey;

    public String placeholder() {
        return columnMybatisPlaceholder.placeholder(property);
    }

    public String wrapColumn() {
        return NameUtils.wrap(column);
    }
}
