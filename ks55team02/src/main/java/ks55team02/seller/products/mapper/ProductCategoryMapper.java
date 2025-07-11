package ks55team02.seller.products.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.seller.products.domain.ProductCategory;

@Mapper
public interface ProductCategoryMapper {

	// 카테고리 ID로 카운트 (중복 체크용)
    int countCategoryById(String categoryId);

    // 카테고리 이름으로 카운트 (중복 체크용)
    int countCategoryByName(String categoryName);

    // 카테고리 정보 수정
    void updateCategory(ProductCategory productCategory);
    
	/*카테고리 활성화*/
    void activateCategory(String categoryId);
    
	/*카테고리 비활성화*/
	void deactivateCategory(String categoryId);

	/*카테고리 추가*/
	void addCategory(ProductCategory productCategory);

	/**
     * 검색 조건에 맞는 카테고리의 총 개수를 조회합니다.
     * @param searchCriteria 검색 조건
     * @return 검색된 카테고리 총 개수
     */
    int getCategoryListCount(SearchCriteria searchCriteria);

    /**
     * 검색 및 페이지네이션 조건에 맞는 카테고리 목록을 조회합니다.
     * @param searchCriteria 검색 및 페이지네이션 조건
     * @return 카테고리 목록
     */
    List<ProductCategory> getCategoryList(SearchCriteria searchCriteria);
	
    /* =======================================================
       ProductsMapper와 기존 ProductCategoryMapper의 모든 카테고리 메소드를 통합
       ======================================================= */

    // 카테고리 ID로 단일 카테고리 정보를 조회
    ProductCategory getCategoryById(String categoryId);

    // 부모 카테고리 ID에 해당하는 하위 카테고리 목록을 조회
    // (메소드명 일관성을 위해 getProductCategoriesByParentId -> getSubCategoriesByParentId 로 변경)
    List<ProductCategory> getSubCategoriesByParentId(String parentCategoryId);

    // 모든 최상위 카테고리 (부모가 없는) 목록을 조회
    // (getPrimaryProductCategories 보다 더 명확한 이름)
    List<ProductCategory> getAllTopLevelCategories();

    // 최상위 카테고리 이름으로 카테고리 ID를 조회
    String getTopLevelCategoryIdByName(String categoryName);

    // 모든 상품 카테고리 목록을 조회
    List<ProductCategory> getAllProductCategories();
}