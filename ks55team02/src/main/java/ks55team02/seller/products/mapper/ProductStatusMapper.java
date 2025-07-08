// 경로: ks55team02/seller/products/mapper/ProductStatusMapper.java
package ks55team02.seller.products.mapper;

import ks55team02.seller.products.domain.ProductStatus;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ProductStatusMapper {
    List<ProductStatus> getStatusListByGdsNo(String gdsNo);
}