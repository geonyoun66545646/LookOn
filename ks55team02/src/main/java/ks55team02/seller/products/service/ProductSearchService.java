package ks55team02.seller.products.service;

import ks55team02.seller.products.domain.Products;
import java.util.List;
import java.util.Map;

public interface ProductSearchService {
	
    List<Products> getSimilarProducts(String categoryId, String currentGdsNo, int limit);

	 /**
     * 메인 슬라이더에 노출할 최근 상품 목록을 조회합니다.
     * @param limit 조회할 개수
     * @return 최근 상품 목록
     */
    List<Products> getRecentProductsForMain(int limit);
	/**
     * 할인 중인 상품 목록을 조회합니다.
     * @return 할인 상품 목록
     */
    List<Products> getSaleProducts();

    /**
     * 최근 등록된 신상품 목록을 조회합니다.
     * @return 신상품 목록
     */
    List<Products> getNewProducts();
    
    List<Products> getAllActiveProductsForCustomer();
    Map<String, Object> getFilteredAndSortedProducts(String categoryId, String sortBy, Map<String, Object> filterParams, int currentPage);
}