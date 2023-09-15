package com.github.developframework.mybatis.extension.core.sql;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import org.apache.ibatis.scripting.xmltags.SqlNode;

import java.util.function.Function;

/**
 * @author qiushui on 2023-09-15.
 */
@FunctionalInterface
public interface SqlCriteria extends SqlPart {

    Function<Interval, SqlNode> toSqlNode();
}
