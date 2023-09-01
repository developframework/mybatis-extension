package com.github.developframework.mybatis.extension.core.structs;

/**
 * 列的构建原数据
 *
 * @author qiushui on 2023-08-30.
 */
public class ColumnBuildMetadata {

    public String customizeType;

    public int length;

    public int scale;

    public boolean unsigned = true;

    public boolean nullable;

    public boolean autoIncrement;

    public String defaultValue;

    public String comment;
}
