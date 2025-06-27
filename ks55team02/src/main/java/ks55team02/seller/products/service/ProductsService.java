package ks55team02.seller.products.service;

import ks55team02.seller.products.domain.Products;
import ks55team02.seller.products.domain.ProductRegistrationRequest;
import java.util.List;

public interface ProductsService {

    /**
     * 판매자 및 스토어 ID에 해당하는 상품 목록을 조회합니다.
     * @param selUserNo 판매자 번호
     * @param storeId 스토어 ID
     * @return 조건에 맞는 상품 목록
     */
    List<Products> getProductsBySellerAndStore(String selUserNo, String storeId);

    /**
     * 새로운 상품을 등록하고 관련 이미지, 옵션, 재고 정보를 처리합니다.
     * @param request 상품 등록에 필요한 모든 정보를 담은 요청 객체
     */
    void addProduct(ProductRegistrationRequest request);

    /**
     * 특정 상품 코드가 이미 존재하는지 확인합니다.
     * @param productCode 확인할 상품 코드
     * @return true면 중복, false면 중복 아님
     */
    boolean isProductCodeDuplicated(String productCode);

    /**
     * 판매자/관리자용: 모든 상품 목록을 조회합니다. (필요시 사용)
     * 현재는 getProductsBySellerAndStore로 대체될 수 있습니다.
     * @return 모든 상품 목록
     */
    List<Products> getProductList(); // 이전 getSellerProductList 대신 더 일반적인 이름으로 변경 또는 유지

    /**
     * 고객용: 활성화되고 노출되는 상품 목록을 조회합니다.
     * @return 활성화되고 노출되는 상품 목록
     */
    List<Products> getAllActiveProductsForCustomer();

    // ✅ 새로운 상품 상세 조회 메서드 추가
    /**
     * 특정 상품의 상세 정보를 조회하고, 연관된 이미지 및 카테고리 정보를 함께 반환합니다.
     * @param gdsNo 조회할 상품 번호
     * @return 상세 정보가 담긴 Products 객체 (이미지 리스트 포함)
     */
    Products getProductDetailWithImages(String gdsNo);
}