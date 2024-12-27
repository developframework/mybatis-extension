package com.github.developframework.mybatis.extension.core.sql.builder;

import com.github.developframework.mybatis.extension.core.structs.ParameterKeys;
import lombok.Getter;
import org.apache.ibatis.binding.MapperMethod;

/**
 * @author qiushui on 2024-12-27.
 */
public class SqlCriteriaBuilderContext {

    @Getter
    private final MapperMethod.ParamMap<Object> criteriaParamMap = new MapperMethod.ParamMap<>();

    private int criteriaParamIndex;

    public String collectParam(Object value) {
        String paramName = ParameterKeys.CRITERIA_PARAM + criteriaParamIndex++;
        criteriaParamMap.put(paramName, value);
        return paramName;
    }
}
