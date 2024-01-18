package com.github.developframework.mybatis.extension.core.parser.naming;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author qiushui on 2023-08-30.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Operate {

    ISNULL("isNull", 0, "%s IS NULL"),

    NOTNULL("notNull", 0, "%s IS NOT NULL"),

    EQ("eq", 1, "%s = %s"),

    NE("ne", 1, "%s != %s"),

    GT("gt", 1, "%s > %s"),

    GTE("gte", 1, "%s >= %s"),

    LT("lt", 1, "%s < %s"),

    LTE("lte", 1, "%s <= %s"),

    BETWEEN("between", 2, "%s BETWEEN %s AND %s"),

    LIKE("like", 1, "%s LIKE CONCAT('%%', %s, '%%')"),

    LIKE_HEAD("likeHead", 1, "%s LIKE CONCAT(%s, '%%')"),

    LIKE_TAIL("likeTail", 1, "%s LIKE CONCAT('%%', %s)"),

    IN("in", 1, "%s IN "),

    NOT_IN("notIn", 1, "%s NOT IN "),

    EQ_TRUE("true", 0, "%s = 1"),

    EQ_FALSE("false", 0, "%s = 0");

    // 关键字
    private final String keyword;

    // 参数个数
    private final int paramCount;

    // 写成SQL的格式
    private final String format;

    public static Set<String> keywords() {
        return Stream.of(Operate.values()).map(Operate::getKeyword).collect(Collectors.toUnmodifiableSet());
    }

    public static Operate of(String str) {
        for (Operate operate : Operate.values()) {
            if (operate.getKeyword().equalsIgnoreCase(str)) {
                return operate;
            }
        }
        throw new IllegalArgumentException();
    }
}
