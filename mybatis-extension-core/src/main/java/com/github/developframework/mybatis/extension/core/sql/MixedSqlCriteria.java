package com.github.developframework.mybatis.extension.core.sql;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaBuilderContext;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.TrimSqlNode;
import org.apache.ibatis.session.Configuration;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author qiushui on 2023-09-15.
 */
public class MixedSqlCriteria extends SqlCriteria {

    private final Interval mixedInterval;

    private final SqlCriteria[] criteriaChain;

    public MixedSqlCriteria(Interval mixedInterval, SqlCriteria[] criteriaChain) {
        this.mixedInterval = mixedInterval;
        this.criteriaChain = criteriaChain;
    }

    @Override
    public SqlNode toSqlNode(Configuration configuration, SqlCriteriaBuilderContext context, Interval interval) {
        final MixedSqlNode mixedSqlNode = new MixedSqlNode(
                Arrays.stream(criteriaChain)
                        .filter(Objects::nonNull)
                        .map(c -> c.toSqlNode(configuration, context, mixedInterval))
                        .collect(Collectors.toList())
        );
        String prefix = null, suffix = null;
        if (interval != Interval.EMPTY) {
            prefix = String.format(" %s (", interval.name());
            suffix = ")";
        }
        return new TrimSqlNode(configuration, mixedSqlNode, prefix, mixedInterval.name(), suffix, null);
    }
}
