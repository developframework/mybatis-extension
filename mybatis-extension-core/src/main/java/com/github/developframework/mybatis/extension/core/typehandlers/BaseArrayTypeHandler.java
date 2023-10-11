package com.github.developframework.mybatis.extension.core.typehandlers;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author qiushui on 2023-10-09.
 */
public abstract class BaseArrayTypeHandler<T> extends BaseTypeHandler<T[]> {

    protected abstract Class<T> getTargetClass();

    protected abstract T convertToArray(String value);

    protected abstract String convertToString(T o);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T[] parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(
                i,
                Arrays.stream(parameter)
                        .map(this::convertToString)
                        .collect(Collectors.joining(","))
        );
    }

    @Override
    public T[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return handleValue(rs.getString(columnName));
    }

    @Override
    public T[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return handleValue(rs.getString(columnIndex));
    }

    @Override
    public T[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return handleValue(cs.getString(columnIndex));
    }

    @SuppressWarnings("unchecked")
    private T[] handleValue(String value) {
        if (value == null) {
            return null;
        } else if (value.isEmpty()) {
            return (T[]) Array.newInstance(getTargetClass(), 0);
        } else {
            return (T[]) Arrays.stream(value.split(","))
                    .map(this::convertToArray)
                    .toArray();
        }
    }
}
