package com.github.developframework.mybatis.extension.core.structs;

import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;

/**
 * @author qiushui on 2023-09-01.
 */
public record MapperMethodParseWrapper(SqlCommandType sqlCommandType, SqlSource sqlSource) {

}
