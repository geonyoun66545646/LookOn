package ks55team02.seller.products.service;

import ks55team02.seller.products.domain.Products;
import ks55team02.seller.products.domain.ProductRegistrationRequest;
import ks55team02.seller.products.domain.ProductCategory;
import ks55team02.seller.products.domain.ProductOptionValue; // ProductOptionValue 임포트 확인
import ks55team02.seller.stores.domain.Stores;

import java.util.List;
import java.util.Map;

/**
 * 상품 관련 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 * 판매자 상품 관리, 상품 등록, 조회 등 다양한 상품 관련 기능을 제공합니다.
 */
public interface ProductsService {
	
    // 특정 판매자 번호와 스토어 ID에 해당하는 상품 목록을 조회합니다.
    List<Products> getProductsBySellerAndStore(String selUserNo, String storeId);
    // 새로운 상품을 시스템에 등록합니다.
    void addProduct(ProductRegistrationRequest request);
    // 특정 상품 코드가 이미 데이터베이스에 존재하는지 확인합니다.
    boolean isProductCodeDuplicated(String productCode);
    // 모든 상품 목록을 조회합니다.
    List<Products> getProductList();
    // 고객에게 노출될 수 있는, 활성화된 모든 상품 목록을 조회합니다.
    List<Products> getAllActiveProductsForCustomer();
    // 고객용: 특정 카테고리에 속하는 활성화 및 노출 상품 목록을 조회합니다.
    List<Products> getActiveProductsForCustomerByCategory(String categoryId);
    // 특정 상품의 상세 정보를 조회하고, 연관된 이미지 및 카테고리 정보까지 반환합니다.
    Products getProductDetailWithImages(String gdsNo);

    // 카테고리 ID를 기반으로 해당 카테고리의 계층 정보를 조회합니다.
    Map<String, Object> getCategoryHierarchy(String categoryId);
    // 최상위 카테고리 이름을 받아 해당 카테고리 ID를 반환합니다.
    String getTopLevelCategoryIdByName(String categoryName);
    // 모든 상품 카테고리 목록을 조회합니다.
    List<ProductCategory> getAllProductCategories();

    // --- ⭐ 변경된 메서드 시그니처 ⭐ ---
    /**
     * 다양한 필터 및 정렬 조건에 따라 상품 목록과 페이지네이션 정보를 조회합니다.
     *
     * @param categoryId 조회할 카테고리 ID (nullable)
     * @param sortBy 정렬 기준 (예: "new", "price_asc", "price_desc")
     * @param filterParams 가격 범위, 성별, 색상 등 기타 필터 파라미터 맵
     * @param currentPage 현재 페이지 번호
     * @return 상품 목록 (List<Products>), 총 상품 개수, 현재 페이지, 마지막 페이지 등을 포함한 Map
     */
    Map<String, Object> getFilteredAndSortedProducts(String categoryId, String sortBy, Map<String, Object> filterParams, int currentPage);

    /**
     * 다양한 필터 및 정렬 조건에 해당하는 상품의 총 개수를 조회합니다.
     * 이 메서드는 주로 getFilteredAndSortedProducts 내부에서 사용됩니다.
     *
     * @param categoryId 조회할 카테고리 ID (nullable)
     * @param sortBy 정렬 기준 (예: "new", "price_asc", "price_desc")
     * @param filterParams 가격 범위, 성별, 색상 등 기타 필터 파라미터 맵 (pageSize, offset 포함될 수 있음)
     * @return 필터링된 상품의 총 개수
     */
    long countFilteredProducts(String categoryId, String sortBy, Map<String, Object> filterParams);
    // --- ⭐ 변경된 메서드 시그니처 끝 ⭐ ---

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