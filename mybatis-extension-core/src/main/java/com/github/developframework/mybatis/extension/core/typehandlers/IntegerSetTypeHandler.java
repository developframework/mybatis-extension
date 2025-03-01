package com.github.developframework.mybatis.extension.core.typehandlers;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author qiushui on 2023-09-19.
 */
@MappedTypes(List.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class IntegerSetTypeHandler extends BaseTypeHandler<Set<Integer>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Set<Integer> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, StringUtils.join(parameter, ","));
    }

    @Override
    public Set<Integer> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        final String str = rs.getString(columnName);
        return str == null ? null : Stream.of(str.split(",")).map(Integer::parseInt).collect(Collectors.toSet());
    }

    @Override
    public Set<Integer> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        final String str = rs.getString(columnIndex);
        return str == null ? null : Stream.of(str.split(",")).map(Integer::parseInt).collect(Collectors.toSet());
    }

    @Override
    public Set<Integer> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        final String str = cs.getString(columnIndex);
        return str == null ? null : Stream.of(str.split(",")).map(Integer::parseInt).collect(Collectors.toSet());
    }
}
