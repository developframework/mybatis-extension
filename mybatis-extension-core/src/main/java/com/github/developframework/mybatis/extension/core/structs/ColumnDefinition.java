package com.github.developframework.mybatis.extension.core.structs;

import com.github.developframework.mybatis.extension.core.autoinject.AutoInjectProvider;
import com.github.developframework.mybatis.extension.core.dialect.MybatisExtensionDialect;
import com.github.developframework.mybatis.extension.core.idgenerator.AutoIncrementIdGenerator;
import com.github.developframework.mybatis.extension.core.idgenerator.IdGenerator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Type;

/**
 * @author qiushui on 2023-08-30.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class ColumnDefinition {

    private final MybatisExtensionDialect dialect;

    private String column;

    private String property;

    private Type propertyType;

    private ColumnBuildMetadata columnBuildMetadata;

    private ColumnMybatisPlaceholder columnMybatisPlaceholder;

    private boolean version;

    private boolean logicDelete;

    private boolean useGeneratedKey;

    private boolean primaryKey;

    private boolean multipleTenant;

    private Class<? extends IdGenerator> idGeneratorClass;

    private Class<? extends AutoInjectProvider> autoInjectProviderClass;


    public boolean isAutoIncrement() {
        return idGeneratorClass == AutoIncrementIdGenerator.class;
    }

    public String placeholder() {
        return columnMybatisPlaceholder.placeholder(property);
    }

    public String wrapColumn() {
        return dialect.columnName(column);
    }
}
