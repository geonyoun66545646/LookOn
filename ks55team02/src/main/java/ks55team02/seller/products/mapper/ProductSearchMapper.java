package ks55team02.seller.products.mapper;

import ks55team02.seller.products.domain.Products;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface ProductSearchMapper {
	List<Products> getRecentProductsForMain(int limit);
	List<Products> getSaleProducts();
    List<Products> getNewProducts();
    List<Products> getSimilarProducts(Map<String, Object> params);
    List<Products> getFilteredAndSortedProducts(Map<String, Object> paramMap);
    long countFilteredProducts(Map<String, Object> paramMap);
    List<Products> getAllActiveProductsForCustomer();
    List<Products> getActiveProductsForCustomerByCategory(String categoryId);

    /**
     * 메인 페이지의 Weekly Best 상품 목록을 조회합니다.
     * @return Weekly Best 상품 목록 (24개)
     */
    List<Products> getWeeklyBestProducts();
    
    /**
     * 특정 할인율 이상인 특가 상품 목록을 조회합니다.
     * @param params minDiscountRate (double)
     * @return 특가 상품 목록
     */
    List<Products> getSpecialSaleProducts(Map<String, Object> params); // ⭐ 이 부분 추가
}