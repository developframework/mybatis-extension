package com.github.developframework.mybatis.extension.core.interceptors.inner;

import com.github.developframework.mybatis.extension.core.BaseMapper;
import com.github.developframework.mybatis.extension.core.interceptors.InnerInterceptor;
import com.github.developframework.mybatis.extension.core.interceptors.InnerInvocation;
import com.github.developframework.mybatis.extension.core.interceptors.InterceptContext;
import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import com.github.developframework.mybatis.extension.core.structs.CompositeId;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.ParameterKeys;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.reflection.ParamNameResolver;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qiushui on 2023-09-19.
 */
public class CompositeIdInnerInterceptor implements InnerInterceptor {

    @Override
    public Object executorUpdate(InnerInvocation innerInvocation, InterceptContext context) throws Throwable {
        if (context.getEntityDefinition().isCompositeId()) {
            Method mapperMethod = context.getMappedStatementMetadata().getMapperMethod();
            if (mapperMethod.getDeclaringClass() == BaseMapper.class && mapperMethod.getName().contains("ById")) {
                // 是BaseMapper里的ById方法才能执行
                final Object[] args = innerInvocation.getInvocation().getArgs();
                args[1] = replaceValue(context.getEntityDefinition(), args[1]);
            }
        }
        return innerInvocation.proceed();
    }

    @Override
    public Object executorQuery(InnerInvocation innerInvocation, InterceptContext context) throws Throwable {
        return executorUpdate(innerInvocation, context);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> replaceValue(EntityDefinition entityDefinition, Object object) {
        final ColumnDefinition[] idColumnDefinitions = entityDefinition.getPrimaryKeyColumnDefinitions();
        if (object instanceof CompositeId) {
            final Map<String, Object> data = ((CompositeId) object).getInnerMap();
            final Map<String, Object> idMap = new HashMap<>();
            for (ColumnDefinition idColumnDefinition : idColumnDefinitions) {
                String property = idColumnDefinition.getProperty();
                idMap.put(property, data.get(property));
            }
            final Map<String, Object> newMap = new HashMap<>();
            newMap.put(ParameterKeys.ID, idMap);
            return newMap;
        } else if (object instanceof Collection || object.getClass().isArray()) {
            return Map.of(ParamNameResolver.GENERIC_NAME_PREFIX + "1", object);
        } else if (object instanceof MapperMethod.ParamMap) {
            final MapperMethod.ParamMap<Object> paramMap = (MapperMethod.ParamMap<Object>) object;
            // 带_lock的方法
            if (paramMap.containsKey(ParameterKeys.LOCK)) {
                Map<String, Object> newMap = new HashMap<>();
                newMap.put(ParameterKeys.LOCK, paramMap.get(ParameterKeys.LOCK));
                if (paramMap.containsKey("id")) {
                    newMap.putAll(replaceValue(entityDefinition, paramMap.get("id")));
                } else if (paramMap.containsKey("ids")) {
                    newMap.putAll(replaceValue(entityDefinition, paramMap.get("ids")));
                }
                return newMap;
            } else {
                return paramMap;
            }
        }
        throw new IllegalArgumentException("复合主键参数不支持类型：" + object.getClass());
    }
}
