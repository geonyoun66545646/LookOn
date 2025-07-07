package ks55team02.seller.products.mapper;

import ks55team02.seller.products.domain.*;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface ProductsMapper {

    // --- 상품 등록(Create) 관련 메소드 ---

    String getMaxProductCode();
    String getMaxImageNo();
    String getMaxOptionNo();
    String getMaxOptionValueNo();
    String getMaxStatusNo();
    void addProduct(Products product);
    void insertProductImage(ProductImage productImage);
    void insertProductOption(ProductOption option);
    void insertProductOptionValue(ProductOptionValue value);
    void insertProductStatus(ProductStatus productStatus);
    void insertStatusOptionMapping(StatusOptionMapping mapping);

    // 상품 수정
    /**
     * 상품의 기본 정보를 수정합니다.
     * @param product 수정할 정보가 담긴 Products 객체
     */
    void updateProduct(Products product);

    /**
     * 특정 상품에 속한 모든 이미지를 비활성화 처리합니다.
     * @param gdsNo 상품 ID
     */
    void deactivateImagesByGdsNo(String gdsNo);

    /**
     * 특정 상품에 속한 모든 옵션을 비활성화 처리합니다.
     * @param gdsNo 상품 ID
     */
    void deactivateOptionsByGdsNo(String gdsNo);

    /**
     * 특정 상품에 속한 모든 재고/상태 정보를 비활성화 처리합니다.
     * @param gdsNo 상품 ID
     */
    void deactivateStatusByGdsNo(String gdsNo);
    
    // -- 상품 삭제
    /**
     * 특정 상품을 비활성화(소프트 삭제) 처리합니다.
     * @param paramMap 'gdsNo', 'selUserNo' (삭제 요청자)를 포함한 맵
     */
    void deactivateProduct(Map<String, Object> paramMap);
    
    // --- 상품 조회(Read) 관련 메소드 (주로 판매자/관리자용) ---
    
    /**
     * 특정 판매자의 특정 스토어에 속한 모든 상품 목록을 조회합니다.
     * @param paramMap 'selUserNo', 'storeId'를 포함한 맵
     * @return 상품 목록
     */
    List<Products> getProductsBySellerAndStore(Map<String, Object> paramMap);

    /**
     * (관리자용) 시스템의 모든 상품 목록을 조회합니다.
     * @return 모든 상품 목록
     */
    List<Products> getProductList();

    /**
     * 특정 상품의 모든 상세 정보를 연관된 객체(이미지, 옵션 등)와 함께 조회합니다.
     * @param gdsNo 조회할 상품의 ID
     * @return 상품 상세 정보 객체
     */
    Products getProductDetailWithImages(String gdsNo);

    // --- 유틸리티 메소드 ---
    
    /**
     * 특정 상품 코드가 이미 데이터베이스에 존재하는지 확인합니다.
     * @param productCode 확인할 상품 코드
     * @return 존재하면 1 이상, 없으면 0
     */
    int countProductCode(String productCode);
}