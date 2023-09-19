package com.github.developframework.mybatis.extension.core.typehandlers;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author qiushui on 2023-09-19.
 */
@MappedTypes(Set.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class StringSetTypeHandler extends BaseTypeHandler<Set<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Set<String> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, String.join(",", parameter));
    }

    @Override
    public Set<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return handleValue(rs.getString(columnName));
    }

    @Override
    public Set<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return handleValue(rs.getString(columnIndex));
    }

    @Override
    public Set<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return handleValue(cs.getString(columnIndex));
    }

    private Set<String> handleValue(String value) {
        if (value == null) {
            return null;
        } else if (value.isEmpty()) {
            return new HashSet<>();
        } else {
            return new HashSet<>(Set.of(value.split(",")));
        }
    }
}
