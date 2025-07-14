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

    /**
     * 다양한 필터 및 정렬 조건에 따라 상품 목록을 조회합니다.
     * @param paramMap 필터링 및 정렬, 페이지네이션 정보가 담긴 Map
     * @return 필터링된 상품 목록
     */
    List<Products> getFilteredAndSortedProducts(Map<String, Object> paramMap);

    /**
     * 다양한 필터 조건에 해당하는 상품의 총 개수를 조회합니다.
     * @param paramMap 필터링 정보가 담긴 Map
     * @return 필터링된 상품의 총 개수
     */
    long countFilteredProducts(Map<String, Object> paramMap);

    /**
     * 고객에게 노출될 수 있는, 활성화된 모든 상품 목록을 조회합니다.
     * @return 활성화된 모든 상품 목록
     */
    List<Products> getAllActiveProductsForCustomer();

    /**
     * 특정 카테고리에 속하는 활성화 및 노출 상품 목록을 조회합니다.
     * @param categoryId 카테고리 ID
     * @return 해당 카테고리의 활성화된 상품 목록
     */
    List<Products> getActiveProductsForCustomerByCategory(String categoryId);
}