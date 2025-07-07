package ks55team02.seller.products.service;

import ks55team02.seller.products.domain.Products;
import ks55team02.seller.products.domain.ProductRegistrationRequest;
import ks55team02.seller.products.domain.ProductOptionValue; // ProductOptionValue 임포트 확인
import ks55team02.seller.stores.domain.Stores;

import java.util.List;

/**
 * 상품 관련 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 * 판매자 상품 관리, 상품 등록, 조회 등 다양한 상품 관련 기능을 제공합니다.
 */
public interface ProductsService {
	
	
	
	/**
     * 특정 상품을 비활성화(소프트 삭제) 합니다.
     * @param gdsNo 비활성화할 상품 ID
     * @param selUserNo 작업을 요청한 판매자 ID
     */
    void deactivateProduct(String gdsNo, String selUserNo);

    /**
     * 기존 상품의 정보를 수정합니다.
     * @param request 수정할 상품 정보가 담긴 DTO
     */
    void updateProduct(ProductRegistrationRequest request);
	
    // 특정 판매자 번호와 스토어 ID에 해당하는 상품 목록을 조회합니다.
    List<Products> getProductsBySellerAndStore(String selUserNo, String storeId);
    // 새로운 상품을 시스템에 등록합니다.
    void addProduct(ProductRegistrationRequest request);
    // 특정 상품 코드가 이미 데이터베이스에 존재하는지 확인합니다.
    boolean isProductCodeDuplicated(String productCode);
    // 모든 상품 목록을 조회합니다.
    List<Products> getProductList();
    // 특정 상품의 상세 정보를 조회하고, 연관된 이미지 및 카테고리 정보까지 반환합니다.
    Products getProductDetailWithImages(String gdsNo);

    

    // 모든 상품 컬러 옵션 값을 조회합니다.
    List<ProductOptionValue> getAllProductColors();
    // 모든 의류 표준 사이즈 옵션 값을 조회합니다.
    List<ProductOptionValue> getAllApparelSizes();
    // 모든 신발 표준 사이즈 옵션 값을 조회합니다.
    List<ProductOptionValue> getAllShoeSizes();
    // ⭐ 모든 패션 소품 표준 사이즈 옵션 값을 조회합니다. (이 줄이 추가되었습니다!) ⭐
    List<ProductOptionValue> getAllFashionSizes(); // <--- 이 라인이 추가되어야 합니다.
    // 모든 브랜드(상점) 목록을 조회합니다.
    List<Stores> getAllBrands();
}