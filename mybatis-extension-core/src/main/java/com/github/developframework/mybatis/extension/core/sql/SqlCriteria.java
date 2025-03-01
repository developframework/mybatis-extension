package com.github.developframework.mybatis.extension.core.sql;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaBuilderContext;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.session.Configuration;

/**
 * @author qiushui on 2023-09-15.
 */
@RequiredArgsConstructor
public abstract class SqlCriteria implements SqlPart {

    public abstract SqlNode toSqlNode(Configuration configuration, SqlCriteriaBuilderContext context, Interval interval);
}
