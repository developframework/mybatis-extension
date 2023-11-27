package com.github.developframework.mybatis.extension.core.structs;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

/**
 * @author qiushui on 2023-08-30.
 */
public class ColumnMybatisPlaceholder {

    public Class<?> javaType;

    public JdbcType jdbcType;

    public Class<? extends TypeHandler<?>> typeHandlerClass;

    public String placeholder(String param) {
        StringBuilder sb = new StringBuilder();
        sb.append("#{");
        sb.append(param);
        if (typeHandlerClass != null && typeHandlerClass != UnknownTypeHandler.class) {
            sb.append(",typeHandler=").append(typeHandlerClass.getName());
        }
        if (javaType != null && javaType != void.class) {
            sb.append(",javaType=").append(javaType);
        }
        if (jdbcType != null && jdbcType != JdbcType.UNDEFINED) {
            sb.append(",jdbcType=").append(jdbcType.name());
        }
        sb.append("}");
        return sb.toString();
    }
}
