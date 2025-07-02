package ks55team02.seller.products.service;

import ks55team02.seller.products.domain.Products; // Products DTO 임포트
import ks55team02.seller.products.domain.ProductRegistrationRequest; // ProductRegistrationRequest DTO 임포트
import java.util.List; // List 인터페이스 임포트

/**
 * 상품 관련 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 * 판매자 상품 관리, 상품 등록, 조회 등 다양한 상품 관련 기능을 제공합니다.
 */
public interface ProductsService {
	
    // 특정 판매자 번호(selUserNo)와 스토어 ID(storeId)에 해당하는 상품 목록을 조회합니다.
    List<Products> getProductsBySellerAndStore(String selUserNo, String storeId);
    
    // 새로운 상품을 시스템에 등록하는 기능을 수행합니다.
    void addProduct(ProductRegistrationRequest request);

    // 특정 상품 코드가 이미 데이터베이스에 존재하는지 여부를 확인합니다.
    boolean isProductCodeDuplicated(String productCode);

    // 모든 상품 목록을 조회합니다.
    List<Products> getProductList();

    // 고객에게 노출될 수 있는, 활성화된 모든 상품 목록을 조회합니다.
    List<Products> getAllActiveProductsForCustomer();

    // 고객용: 특정 카테고리에 속하는 활성화 및 노출 상품 목록을 조회합니다.
    List<Products> getActiveProductsForCustomerByCategory(String categoryId); // <--- 이 부분이 추가되었습니다.

    // 특정 상품의 상세 정보를 조회하고, 해당 상품과 연관된 이미지 및 카테고리 정보까지 함께 반환합니다.
    Products getProductDetailWithImages(String gdsNo);
}