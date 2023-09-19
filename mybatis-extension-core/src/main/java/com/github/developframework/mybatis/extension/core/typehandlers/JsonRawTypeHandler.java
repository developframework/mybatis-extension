package com.github.developframework.mybatis.extension.core.typehandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.developframework.mybatis.extension.core.typehandlers.types.JsonRaw;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author qiushui on 2023-09-19.
 */
@RequiredArgsConstructor
public class JsonRawTypeHandler extends BaseTypeHandler<JsonRaw<?>> {

    private final ObjectMapper objectMapper;

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JsonRaw<?> parameter, JdbcType jdbcType) throws SQLException {
        try {
            final String json = objectMapper.writeValueAsString(parameter.getValue());
            ps.setString(i, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonRaw<?> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        final String json = rs.getString(columnName);
        if (json == null) {
            return null;
        }
        return null;
    }

    @Override
    public JsonRaw<?> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public JsonRaw<?> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return null;
    }
}
