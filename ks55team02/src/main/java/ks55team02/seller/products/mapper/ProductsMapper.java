package ks55team02.seller.products.mapper;

import ks55team02.seller.products.domain.Products;
import ks55team02.seller.products.domain.ProductImage;
import ks55team02.seller.products.domain.ProductOption;
import ks55team02.seller.products.domain.ProductOptionValue;
import ks55team02.seller.products.domain.ProductStatus;
// 필요한 경우 ProductStatusOptionMapping 추가
// import ks55team02.seller.products.domain.StatusOptionMapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductsMapper {
    /**
     * 판매자/관리자용: 모든 상품 목록을 조회합니다.
     * 이 메서드는 ProductsMapper.xml에 정의된 'getProductList' 쿼리와 매핑됩니다.
     * @return 모든 상품 목록 (Products 객체 리스트)
     */
    List<Products> getProductList();
    
    // ✅ 고객용: 활성화되고 노출되는 상품 목록 조회
    List<Products> getAllActiveProductsForCustomer();
    
    // ✅ 판매자 번호와 스토어 ID로 상품 목록 조회 메서드를 추가합니다.
    List<Products> getProductsBySellerAndStore(Map<String, Object> paramMap);
    
    // 전체 판매 상품 목록 조회 (누락된 메서드)
    List<Products> getSellerProductList();
    
    // 상품 코드의 최대값 가져오기 (예: GDS_1 -> GDS_2)
    String getMaxProductCode();

    // 상품 등록
    void addProduct(Products product);

    // 이미지 코드의 최대값 가져오기 (예: IMG_1 -> IMG_2)
    String getMaxImageNo();

    // 📌 상품 이미지 등록 (img_type 포함) - 파라미터는 ProductImage 객체 그대로
    void insertProductImage(ProductImage productImage);

    // 📌 상품 상세 정보 및 연관된 이미지 리스트, 카테고리 정보를 함께 조회
    // 이 메서드는 ProductImage 리스트를 포함한 Products 객체를 반환합니다.
    Products getProductDetailWithImages(@Param("gdsNo") String gdsNo);

    // 옵션 코드의 최대값 가져오기 (예: OPT_1 -> OPT_2)
    String getMaxOptionNo();

    // 상품 옵션 등록
    void insertProductOption(ProductOption productOption);

    // 옵션 값 코드의 최대값 가져오기 (예: OPTVL_1 -> OPTVL_2)
    String getMaxOptionValueNo();

    // 상품 옵션 값 등록
    void insertProductOptionValue(ProductOptionValue productOptionValue);

    // 상품 상태 코드의 최대값 가져오기 (예: PSTS_1 -> PSTS_2)
    String getMaxStatusNo();

    // 상품 상태 등록
    void insertProductStatus(ProductStatus productStatus);

    // 상품 코드 중복 확인 (필요한 경우)
    int countProductCode(@Param("productCode") String productCode);

    // 📌 추가: 이미지 업데이트 (필요 시)
    void updateProductImage(ProductImage productImage);

    // 📌 추가: 특정 상품에 대한 이미지 리스트만 별도로 조회 (필요 시)
    List<ProductImage> getImagesByGdsNo(@Param("gdsNo") String gdsNo);

    // 기타 getMax* 메서드들은 그대로 유지합니다.
    String getNewGdsNo(); // XML에 정의되어 있지만, 인터페이스에 없던 부분
    String getMaxStatusOptionMappingNo(); // XML에 정의되어 있지만, 인터페이스에 없던 부분
    void insertStatusOptionMapping(Map<String, Object> paramMap); // XML에 정의되어 있지만, 인터페이스에 없던 부분 (StatusOptionMapping 객체로 변경 권장)
}