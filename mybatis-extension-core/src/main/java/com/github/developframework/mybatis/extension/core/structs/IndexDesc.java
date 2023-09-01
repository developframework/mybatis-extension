package com.github.developframework.mybatis.extension.core.structs;

import lombok.Getter;
import lombok.Setter;

/**
 * @author qiushui on 2023-09-01.
 */
@Getter
@Setter
public class IndexDesc {

    private boolean NonUnique;

    private String KeyName;

    private String ColumnName;

    private IndexMode IndexType;
}
