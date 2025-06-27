package ks55team02.seller.products.handler;

import ks55team02.seller.products.domain.ProductImageType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(ProductImageType.class)
public class ProductImageTypeHandler extends BaseTypeHandler<ProductImageType> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ProductImageType parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public ProductImageType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int code = rs.getInt(columnName);
        return rs.wasNull() ? null : ProductImageType.fromCode(code);
    }

    @Override
    public ProductImageType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int code = rs.getInt(columnIndex);
        return rs.wasNull() ? null : ProductImageType.fromCode(code);
    }

    @Override
    public ProductImageType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int code = cs.getInt(columnIndex);
        return cs.wasNull() ? null : ProductImageType.fromCode(code);
    }
}