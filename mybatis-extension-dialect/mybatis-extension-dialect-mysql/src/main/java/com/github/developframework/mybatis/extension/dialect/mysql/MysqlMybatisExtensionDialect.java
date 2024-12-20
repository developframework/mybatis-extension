package com.github.developframework.mybatis.extension.dialect.mysql;

import com.github.developframework.mybatis.extension.core.dialect.ColumnDescription;
import com.github.developframework.mybatis.extension.core.dialect.MybatisExtensionDialect;
import com.github.developframework.mybatis.extension.core.dialect.SqlSourceBuilder;
import com.github.developframework.mybatis.extension.core.structs.ColumnBuildMetadata;
import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author qiushui on 2024-12-19.
 */
public class MysqlMybatisExtensionDialect implements MybatisExtensionDialect {

    @Override
    public String tableName(String originalTableName) {
        return "`" + originalTableName + "`";
    }

    @Override
    public String columnName(String originalColumnName) {
        return "`" + originalColumnName + "`";
    }

    @Override
    public String literal(String value) {
        return "'" + value + "'";
    }

    @Override
    public SqlSourceBuilder sqlFragmentBuilder() {
        return MysqlSqlSourceBuilder.INSTANCE;
    }

    @Override
    public ColumnDescription buildColumnDescriptionByColumnDefinition(ColumnDefinition columnDefinition) {
        ColumnBuildMetadata columnBuildMetadata = columnDefinition.getColumnBuildMetadata();
        MysqlColumnDescription columnDesc = new MysqlColumnDescription();
        columnDesc.setField(columnDefinition.getColumn());
        columnDesc.setType(columnType(columnBuildMetadata, columnDefinition.getPropertyType()));
        columnDesc.setNull(columnBuildMetadata.nullable ? "YES" : "NO");
        columnDesc.setKey(columnDefinition.isPrimaryKey() ? "PRI" : "");
        columnDesc.setDefault(columnBuildMetadata.nullable ? columnBuildMetadata.defaultValue : null);
        columnDesc.setExtra(columnDefinition.isAutoIncrement() ? "auto_increment" : "");
        columnDesc.setComment(columnBuildMetadata.comment);
        return columnDesc;
    }

    private String columnType(ColumnBuildMetadata columnBuildMetadata, java.lang.reflect.Type propertyType) {
        if (StringUtils.isNotEmpty(columnBuildMetadata.customizeType)) {
            final int i = columnBuildMetadata.customizeType.indexOf("(");
            if (i < 0) {
                return columnBuildMetadata.customizeType.toLowerCase();
            } else {
                return columnBuildMetadata.customizeType.substring(0, i).toLowerCase() + StringUtils.deleteWhitespace(columnBuildMetadata.customizeType.substring(i));
            }
        }
        Class<?> clazz;
        if (propertyType instanceof Class) {
            clazz = (Class<?>) propertyType;
        } else if (propertyType instanceof ParameterizedType) {
            clazz = (Class<?>) ((ParameterizedType) propertyType).getRawType();
        } else {
            throw new IllegalArgumentException();
        }
        String type;
        int finalLength = 0;
        int finalScale = 0;
        boolean finalUnsigned = false;
        if (clazz == String.class) {
            type = "varchar";
            finalLength = 100;
        } else if (clazz == Integer.class || clazz == Integer.TYPE) {
            type = "int";
            finalUnsigned = columnBuildMetadata.unsigned;
        } else if (clazz == Long.class || clazz == Long.TYPE) {
            type = "bigint";
            finalUnsigned = columnBuildMetadata.unsigned;
        } else if (clazz == Boolean.class || clazz == Boolean.TYPE) {
            type = "bit";
            finalLength = 1;
        } else if (clazz == Float.class || clazz == Float.TYPE) {
            type = "float";
            finalLength = 6;
            finalScale = 2;
            finalUnsigned = columnBuildMetadata.unsigned;
        } else if (clazz == Double.class || clazz == Double.TYPE) {
            type = "double";
            finalLength = 12;
            finalScale = 2;
            finalUnsigned = columnBuildMetadata.unsigned;
        } else if (clazz == BigDecimal.class) {
            type = "decimal";
            finalLength = 10;
            finalScale = 2;
            finalUnsigned = columnBuildMetadata.unsigned;
        } else if (
                clazz == LocalDateTime.class ||
                        clazz == ZonedDateTime.class ||
                        clazz == java.util.Date.class ||
                        clazz == java.util.Calendar.class
        ) {
            type = "datetime";
        } else if (clazz == LocalDate.class || clazz == java.sql.Date.class) {
            type = "date";
        } else if (clazz == LocalTime.class) {
            type = "time";
        } else if (clazz == java.sql.Timestamp.class) {
            type = "timestamp";
        } else if (clazz.isEnum()) {
            type = Arrays.stream(clazz.getEnumConstants())
                    .map(v -> literal(v.toString()))
                    .collect(Collectors.joining(",", "enum(", ")"));
        } else {
            type = "varchar";
            finalLength = 100;
        }

        if (columnBuildMetadata.length != 0) {
            finalLength = columnBuildMetadata.length;
        }
        if (columnBuildMetadata.scale != 0) {
            finalScale = columnBuildMetadata.scale;
        }

        if (finalLength != 0) {
            type += ("(" + finalLength);
            if (finalScale != 0) {
                type += ("," + finalScale);
            }
            type += ")";
        }
        if (finalUnsigned) {
            type += " unsigned";
        }
        return type;
    }
}
