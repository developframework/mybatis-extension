package test.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import test.entity.GoodsSpec;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * @author qiushui on 2023-11-24.
 */
@MappedTypes(GoodsSpec[].class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class GoodsSpecArrayTypeHandler extends BaseTypeHandler<GoodsSpec[]> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, GoodsSpec[] parameter, JdbcType jdbcType) throws SQLException {

    }

    @Override
    public GoodsSpec[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        final String str = rs.getString(columnName);
        return str == null ? null : Arrays.stream(str.split(",")).map(GoodsSpec::of).toArray(GoodsSpec[]::new);
    }

    @Override
    public GoodsSpec[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        final String str = rs.getString(columnIndex);
        return str == null ? null : Arrays.stream(str.split(",")).map(GoodsSpec::of).toArray(GoodsSpec[]::new);
    }

    @Override
    public GoodsSpec[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        final String str = cs.getString(columnIndex);
        return str == null ? null : Arrays.stream(str.split(",")).map(GoodsSpec::of).toArray(GoodsSpec[]::new);
    }
}
