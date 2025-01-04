package com.github.developframework.mybatis.extension.core.sql.criteria;

/**
 * @author qiushui on 2025-01-03.
 */
public record CriteriaParameter(
        CriteriaParameterType type,
        String paramName,
        String finalValue,
        Object instance
) {

    public String ognlNeqNull() {
        String test;
        if (type == CriteriaParameterType.LITERAL && instance instanceof String) {
            test = paramName + " neq null and " + paramName + " neq ''";
        } else {
            test = paramName + " neq null";
        }
        return test;
    }

    public enum CriteriaParameterType {

        // 字面量
        LITERAL,

        // 列
        FIELD,

        // 函数
        FUNCTION
    }
}
