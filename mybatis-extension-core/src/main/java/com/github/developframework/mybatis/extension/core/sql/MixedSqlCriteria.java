package com.github.developframework.mybatis.extension.core.sql;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.scripting.xmltags.TrimSqlNode;
import org.apache.ibatis.session.Configuration;

import java.util.Arrays;
import java.util.List;
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
            if (_interval == Interval.EMPTY) {
                return new TrimSqlNode(configuration, mixedSqlNode, null, interval.name(), null, null);
            } else {
                return new MixedSqlNode(
                        List.of(
                                new StaticTextSqlNode(_interval.getText()),
                                new TrimSqlNode(configuration, mixedSqlNode, "(", interval.name(), ")", null)
                        )
                );
            }
        };
    }
}
