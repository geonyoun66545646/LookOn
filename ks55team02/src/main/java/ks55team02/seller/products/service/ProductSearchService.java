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
}