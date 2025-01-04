package com.github.developframework.mybatis.extension.core.sql.builder;

import com.github.developframework.mybatis.extension.core.sql.SqlField;
import com.github.developframework.mybatis.extension.core.sql.SqlFunction;
import com.github.developframework.mybatis.extension.core.sql.criteria.CriteriaParameter;
import com.github.developframework.mybatis.extension.core.structs.ParameterKeys;
import com.github.developframework.mybatis.extension.core.utils.NameUtils;
import lombok.Getter;
import org.apache.ibatis.binding.MapperMethod;

/**
 * @author qiushui on 2024-12-27.
 */
public class SqlCriteriaBuilderContext {

    @Getter
    private final MapperMethod.ParamMap<Object> criteriaParamMap = new MapperMethod.ParamMap<>();

    private int criteriaParamIndex;

    public CriteriaParameter newParameter(Object value) {
        CriteriaParameter.CriteriaParameterType type;
        String paramName, finalValue;
        if (value instanceof SqlField sf) {
            type = CriteriaParameter.CriteriaParameterType.FIELD;
            paramName = null;
            finalValue = sf.toSql();
        } else if (value instanceof SqlFunction sf) {
            type = CriteriaParameter.CriteriaParameterType.FUNCTION;
            paramName = null;
            finalValue = sf.toSql();
        } else {
            type = CriteriaParameter.CriteriaParameterType.LITERAL;
            paramName = collectParam(value);
            finalValue = NameUtils.placeholder(paramName);
        }
        return new CriteriaParameter(type, paramName, finalValue, value);
    }

    private String collectParam(Object value) {
        String paramName = ParameterKeys.CRITERIA_PARAM + criteriaParamIndex++;
        criteriaParamMap.put(paramName, value);
        return paramName;
    }
}
