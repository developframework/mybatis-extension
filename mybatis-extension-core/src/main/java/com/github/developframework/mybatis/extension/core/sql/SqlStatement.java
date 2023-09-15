package com.github.developframework.mybatis.extension.core.sql;

import lombok.Getter;
import lombok.Setter;

/**
 * @author qiushui on 2023-09-15.
 */
@Getter
@Setter
public class SqlStatement implements SqlPart {

    private SqlFieldPart[] selectParts;

    private SqlCriteria whereCriteria;

}
