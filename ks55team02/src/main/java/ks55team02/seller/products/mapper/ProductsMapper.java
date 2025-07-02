package ks55team02.seller.products.mapper;

import ks55team02.seller.products.domain.Products;
import ks55team02.seller.products.domain.StatusOptionMapping;
import ks55team02.seller.products.domain.ProductImage;
import ks55team02.seller.products.domain.ProductOption;
import ks55team02.seller.products.domain.ProductOptionValue;
import ks55team02.seller.products.domain.ProductStatus;
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
    
    // 고객용: 활성화되고 노출되는 상품 목록 조회
    List<Products> getAllActiveProductsForCustomer();
    
    // 고객용: 카테고리별 활성화 및 노출 상품 목록 조회
    List<Products> getActiveProductsForCustomerByCategory(@Param("categoryId") String categoryId);
    
    // 판매자 번호와 스토어 ID로 상품 목록 조회 메서드를 추가합니다.
    List<Products> getProductsBySellerAndStore(Map<String, Object> paramMap);
    
    // 전체 판매 상품 목록 조회
    List<Products> getSellerProductList();
    
    // 상품 코드의 최대값 가져오기 (예: GDS_1 -> GDS_2)
    String getMaxProductCode();

    // 상품 등록
    void addProduct(Products product);
    
    // ProductStatus 저장 메서드 (중복 제거)
    void insertProductStatus(ProductStatus productStatus);

    // StatusOptionMapping 저장 메서드 (Map 방식 제거, DTO 방식만 유지)
    void insertStatusOptionMapping(StatusOptionMapping statusOptionMapping);
    
    // 이미지 코드의 최대값 가져오기 (예: IMG_1 -> IMG_2)
    String getMaxImageNo();

    // 상품 이미지 등록 (img_type 포함)
    void insertProductImage(ProductImage productImage);

    // 상품 상세 정보 및 연관된 이미지 리스트, 카테고리 정보를 함께 조회
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
    
    // 상품 코드 중복 확인
    int countProductCode(@Param("productCode") String productCode);

    // 이미지 업데이트
    void updateProductImage(ProductImage productImage);

    // 특정 상품에 대한 이미지 리스트만 별도로 조회
    List<ProductImage> getImagesByGdsNo(@Param("gdsNo") String gdsNo);

    // 기타 getMax* 메서드들
    String getNewGdsNo();
    String getMaxStatusOptionMappingNo();
}