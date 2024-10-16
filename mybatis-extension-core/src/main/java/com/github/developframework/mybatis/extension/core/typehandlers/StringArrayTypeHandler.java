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

/**
 * @author qiushui on 2023-09-19.
 */
@MappedTypes(String[].class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class StringArrayTypeHandler extends BaseTypeHandler<String[]> {


    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String[] parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, StringUtils.join(parameter, ","));
    }

    @Override
    public String[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        final String str = rs.getString(columnName);
        if (str == null) {
            return null;
        } else if (str.isEmpty()) {
            return new String[0];
        } else {
            return str.split(",");
        }
    }

    @Override
    public String[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        final String str = rs.getString(columnIndex);
        if (str == null) {
            return null;
        } else if (str.isEmpty()) {
            return new String[0];
        } else {
            return str.split(",");
        }
    }

    @Override
    public String[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        final String str = cs.getString(columnIndex);
        if (str == null) {
            return null;
        } else if (str.isEmpty()) {
            return new String[0];
        } else {
            return str.split(",");
        }
    }
}
