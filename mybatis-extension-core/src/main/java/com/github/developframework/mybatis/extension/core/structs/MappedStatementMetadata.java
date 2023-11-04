package com.github.developframework.mybatis.extension.core.structs;

import com.github.developframework.mybatis.extension.core.BaseMapper;
import com.github.developframework.mybatis.extension.core.utils.MybatisUtils;
import lombok.*;
import org.apache.ibatis.mapping.MappedStatement;

import java.lang.reflect.Method;

/**
 * MappedStatement 元数据
 *
 * @author qiushui on 2023-08-30.
 */
@Getter
@Setter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MappedStatementMetadata {

    private final Class<?> mapperClass;

    private final Class<?> entityClass;

    private final Method mapperMethod;

    private final MappedStatement originalMappedStatement;

    @SneakyThrows({ClassNotFoundException.class, NoSuchMethodException.class})
    public static MappedStatementMetadata parse(MappedStatement ms) {
        final String msId = ms.getId();
        final int pos = msId.lastIndexOf(".");
        final String methodName = msId.substring(pos + 1);
        final String interfaceName = msId.substring(0, pos);
        final Class<?> mapperClass = Class.forName(interfaceName);
        final Method mapperMethod = getMethodByName(mapperClass, methodName);
        final Class<?> entityClass;
        if (BaseMapper.class.isAssignableFrom(mapperClass)) {
            entityClass = MybatisUtils.getEntityClass(mapperClass);
        } else {
            entityClass = null;
        }
        return new MappedStatementMetadata(mapperClass, entityClass, mapperMethod, ms);
    }

    private static Method getMethodByName(Class<?> mapperClass, String methodName) throws NoSuchMethodException {
        for (Method method : mapperClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new NoSuchMethodException(mapperClass + "." + methodName);
    }

    public boolean isMethodName(String name) {
        return mapperMethod.getName().equals(name);
    }
}
