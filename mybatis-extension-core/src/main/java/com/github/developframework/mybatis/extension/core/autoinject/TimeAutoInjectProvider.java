package com.github.developframework.mybatis.extension.core.autoinject;

import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;

import java.lang.reflect.Type;
import java.time.*;

/**
 * @author qiushui on 2023-09-18.
 */
public abstract class TimeAutoInjectProvider implements AutoInjectProvider {

    @Override
    public final Object provide(EntityDefinition entityDefinition, ColumnDefinition columnDefinition, Object entity) {
        final Type fieldType = columnDefinition.getPropertyType();
        if (fieldType == LocalDateTime.class) {
            return LocalDateTime.now();
        } else if (fieldType == ZonedDateTime.class) {
            return ZonedDateTime.now();
        } else if (fieldType == LocalDate.class) {
            return LocalDate.now();
        } else if (fieldType == LocalTime.class) {
            return LocalTime.now();
        } else if (fieldType == Instant.class) {
            return Instant.now();
        } else if (fieldType == java.util.Date.class) {
            return new java.util.Date();
        } else if (fieldType == java.sql.Date.class) {
            return new java.sql.Date(Instant.now().toEpochMilli());
        } else if (fieldType == java.sql.Timestamp.class) {
            return new java.sql.Timestamp(Instant.now().toEpochMilli());
        } else {
            return null;
        }
    }
}
