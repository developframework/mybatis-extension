package com.github.developframework.mybatis.extension.core.sql;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.TrimSqlNode;
import org.apache.ibatis.session.Configuration;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author qiushui on 2023-09-15.
 */
public class MixedSqlCriteria extends SqlCriteria {

    private final Interval interval;

    private final SqlCriteria[] criteriaChain;

    public MixedSqlCriteria(Configuration configuration, Interval interval, SqlCriteria[] criteriaChain) {
        super(configuration);
        this.interval = interval;
        this.criteriaChain = criteriaChain;
    }

    @Override
    public Function<Interval, SqlNode> toSqlNode() {
        return _interval -> {
            final MixedSqlNode mixedSqlNode = new MixedSqlNode(
                    Arrays.stream(criteriaChain)
                            .filter(Objects::nonNull)
                            .map(c -> c.toSqlNode().apply(interval))
                            .collect(Collectors.toList())
            );
            String prefix = null, suffix = null;
            if (_interval != Interval.EMPTY) {
                prefix = String.format(" %s (", _interval.name());
                suffix = ")";
            }
            return new TrimSqlNode(configuration, mixedSqlNode, prefix, interval.name(), suffix, null);
        };
    }
}
