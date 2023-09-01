package com.github.developframework.mybatis.extension.core.utils;

import com.github.developframework.mybatis.extension.core.BaseMapper;
import com.github.developframework.mybatis.extension.core.parser.MapperMethodParseException;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 工具类
 *
 * @author qiushui on 2023-08-30.
 */
public abstract class MybatisUtils {

    /**
     * 获取BaseMapper上的泛型类型
     */
    public static Class<?> getEntityClass(Class<?> mapperClass) {
        Type[] types = mapperClass.getGenericInterfaces();
        Class<?> entityClass = null;
        for (Type type : types) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                if (parameterizedType.getRawType() == BaseMapper.class) {
                    entityClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                    break;
                }
            }
        }
        return entityClass;
    }

    /**
     * 获取参数内的值
     * 接口方法一个参数时为本身，多个参数时为Map
     *
     * @see org.apache.ibatis.reflection.ParamNameResolver#getNamedParams(Object[])
     */
    @SuppressWarnings("unchecked")
    public static <T> T find(Object object, Class<T> targetClass) {
        if (object != null) {
            if (targetClass.isAssignableFrom(object.getClass())) {
                return (T) object;
            } else if (object instanceof Map) {
                return find(((Map<?, Object>) object).values(), targetClass);
            } else if (object instanceof Collection) {
                for (Object item : ((Collection<?>) object)) {
                    final T t = find(item, targetClass);
                    if (t != null) {
                        return t;
                    }
                }
            }
        }
        return null;
    }

    public static <T> List<T> findAll(Object object, Class<T> targetClass) {
        List<T> results = new LinkedList<>();
        recursiveFind(object, targetClass, results);
        return results;
    }

    /**
     * 递归获取所有指定类型的实例
     */
    @SuppressWarnings("unchecked")
    private static <T> void recursiveFind(Object object, Class<?> targetClass, List<T> entities) {
        if (object != null) {
            if (targetClass.isAssignableFrom(object.getClass())) {
                entities.add((T) object);
            } else if (object instanceof Map) {
                recursiveFind(((Map<String, Object>) object).values(), targetClass, entities);
            } else if (object instanceof Collection) {
                for (Object item : (Collection<?>) object) {
                    recursiveFind(item, targetClass, entities);
                }
            }
        }
    }

    /**
     * 这段的意思是如果方法入参个数为1时，集合的变量名为collection List的变量名为list 数组的变量名为array
     * 参考mybatis源码如下
     *
     * @see org.apache.ibatis.reflection.ParamNameResolver#wrapToMapIfCollection(Object, String)
     */
    public static String getCollectionExpression(Method method, String defaultName) {
        String collectionExpression = defaultName;
        if (method.getParameterCount() == 1) {
            final Class<?> type = method.getParameterTypes()[0];
            if (Collection.class.isAssignableFrom(type)) {
                collectionExpression = List.class.isAssignableFrom(type) ? "list" : "collection";
            } else if (type.isArray()) {
                collectionExpression = "array";
            } else {
                throw new MapperMethodParseException("使用forEach时对应的参数类型不是集合或数组：" + method.getName());
            }
        }
        return collectionExpression;
    }
}
