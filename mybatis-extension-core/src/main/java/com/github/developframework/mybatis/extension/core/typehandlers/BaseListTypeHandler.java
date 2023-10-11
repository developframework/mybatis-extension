package com.github.developframework.mybatis.extension.core.typehandlers;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author qiushui on 2023-10-09.
 */
public abstract class BaseListTypeHandler<T> extends BaseTypeHandler<List<T>> {

    protected abstract T convertToArray(String value);

    protected abstract String convertToString(T o);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<T> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(
                i,
                parameter.stream()
                        .map(this::convertToString)
                        .collect(Collectors.joining(","))
        );
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return handleValue(rs.getString(columnName));
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return handleValue(rs.getString(columnIndex));
    }

    @Override
    public List<T> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return handleValue(cs.getString(columnIndex));
    }

    private List<T> handleValue(String value) {
        if (value == null) {
            return null;
        } else if (value.isEmpty()) {
            return new ArrayList<>();
        } else {
            return Arrays.stream(value.split(","))
                    .map(this::convertToArray)
                    .collect(Collectors.toList());
        }
    }
}
