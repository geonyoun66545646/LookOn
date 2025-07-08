package ks55team02.seller.products.mapper;

import ks55team02.seller.products.domain.ProductImage;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ProductImageMapper {

    /**
     * 특정 상품 번호(gdsNo)에 해당하는 모든 이미지 목록을 조회합니다.
     * @param gdsNo 상품 번호
     * @return 해당 상품의 이미지 목록
     */
    List<ProductImage> getImagesByGdsNo(String gdsNo);
}