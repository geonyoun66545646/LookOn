package ks55team02.seller.products.service;

import ks55team02.seller.products.domain.Products; // Products DTO 임포트
import ks55team02.seller.products.domain.ProductRegistrationRequest; // ProductRegistrationRequest DTO 임포트
import java.util.List; // List 인터페이스 임포트

/**
 * 상품 관련 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 * 판매자 상품 관리, 상품 등록, 조회 등 다양한 상품 관련 기능을 제공합니다.
 */
public interface ProductsService {
	
    /**
     * 특정 판매자 번호(selUserNo)와 스토어 ID(storeId)에 해당하는 상품 목록을 조회합니다.
     * 판매자 대시보드 등에서 특정 판매자의 상품만 보여줄 때 사용됩니다.
     * * @param selUserNo 판매자 고유 번호
     * @param storeId 스토어 고유 ID
     * @return 주어진 조건에 맞는 상품(Products) 객체들의 리스트
     */
    List<Products> getProductsBySellerAndStore(String selUserNo, String storeId);
    
    /**
     * 새로운 상품을 시스템에 등록하는 기능을 수행합니다.
     * 상품의 기본 정보, 이미지, 옵션, 그리고 옵션 조합별 재고 수량까지 모두 포함하여 처리합니다.
     * 이 메서드는 복합적인 DB 작업을 포함하며 트랜잭션으로 관리되어야 합니다.
     * * @param request 상품 등록에 필요한 모든 정보(상품명, 가격, 옵션, 재고 등)를 담고 있는 요청 DTO
     */
    void addProduct(ProductRegistrationRequest request);

    /**
     * 특정 상품 코드가 이미 데이터베이스에 존재하는지 여부를 확인합니다.
     * 상품 등록 시 상품 코드 중복을 방지하기 위해 사용될 수 있습니다.
     * * @param productCode 확인할 상품 코드 문자열
     * @return 상품 코드가 이미 존재하면 true, 존재하지 않으면 false
     */
    boolean isProductCodeDuplicated(String productCode);

    /**
     * 모든 상품 목록을 조회합니다.
     * 이 메서드는 관리자 페이지 등에서 전체 상품을 관리할 때 유용할 수 있습니다.
     * 현재는 `getProductsBySellerAndStore` 메서드로 대체될 수 있는 경우가 많습니다.
     * * @return 시스템에 등록된 모든 상품(Products) 객체들의 리스트
     */
    List<Products> getProductList();

    /**
     * 고객에게 노출될 수 있는, 활성화된 모든 상품 목록을 조회합니다.
     * 품절되지 않고, 비활성화되지 않은 상품들을 필터링하여 반환합니다.
     * * @return 활성화되고 고객에게 노출 가능한 상품(Products) 객체들의 리스트
     */
    List<Products> getAllActiveProductsForCustomer();

    /**
     * 특정 상품의 상세 정보를 조회하고, 해당 상품과 연관된 이미지 및 카테고리 정보까지 함께 반환합니다.
     * 상품 상세 페이지를 구성할 때 사용됩니다.
     * * @param gdsNo 조회할 상품의 고유 번호
     * @return 조회된 상품의 상세 정보와 연관된 이미지 리스트를 포함하는 Products 객체. 해당 상품이 없으면 null 반환 가능.
     */
    Products getProductDetailWithImages(String gdsNo);
}