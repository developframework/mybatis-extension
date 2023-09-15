package com.github.developframework.mybatis.extension.core.sql;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author qiushui on 2023-09-15.
 */
@RequiredArgsConstructor
public class MixedSqlCriteria implements SqlCriteria {

    private final Interval interval;

    private final SqlCriteria[] criteriaChain;

    @Override
    public Function<Interval, SqlNode> toSqlNode() {
        return i ->
                new MixedSqlNode(
                        Arrays.stream(criteriaChain)
                                .map(c -> c.toSqlNode().apply(interval))
                                .collect(Collectors.toList())
                );
    }
}
