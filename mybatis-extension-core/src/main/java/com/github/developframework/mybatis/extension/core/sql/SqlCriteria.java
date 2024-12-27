package com.github.developframework.mybatis.extension.core.sql;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaBuilderContext;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.session.Configuration;

import java.util.function.Function;

/**
 * @author qiushui on 2023-09-15.
 */
@RequiredArgsConstructor
public abstract class SqlCriteria implements SqlPart {

    public abstract Function<Interval, SqlNode> toSqlNode(Configuration configuration, SqlCriteriaBuilderContext context);
}
