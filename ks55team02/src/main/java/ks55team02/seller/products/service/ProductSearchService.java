package ks55team02.seller.products.service;

import ks55team02.seller.products.domain.Products;
import java.util.List;
import java.util.Map;

public interface ProductSearchService {
	
    List<Products> getSimilarProducts(String categoryId, String currentGdsNo);
    List<Products> getRecentProductsForMain(int limit);
    List<Products> getSaleProducts();
    List<Products> getNewProducts();
    List<Products> getAllActiveProductsForCustomer();
    Map<String, Object> getFilteredAndSortedProducts(String categoryId, String sortBy, Map<String, Object> filterParams, int currentPage);
    
    /**
     * 메인 페이지의 Weekly Best 상품 목록을 조회합니다.
     * @return Weekly Best 상품 목록 (24개)
     */
    List<Products> getWeeklyBestProducts();
    
    /**
     * 메인 페이지의 '놓치기 아까운 특가 상품' 목록을 조회합니다.
     * (30% 이상 할인된 상품을 할인율 높은 순으로 정렬)
     * @param minDiscountRate 최소 할인율 (예: 30.0)
     * @param limit 조회할 상품 개수 제한
     * @return 특가 상품 목록
     */
    List<Products> getSpecialSaleProducts(double minDiscountRate); // ⭐ 이 부분 추가
    
    /**
     * 특정 브랜드의 최신 상품 목록을 조회합니다 (브랜드 스냅용).
     * @param storeId 상점 고유 ID
     * @param limit 조회할 상품 개수
     * @return 해당 브랜드의 최신 상품 목록
     */
    List<Products> getRecentProductsByStoreId(String storeId, int limit);
}