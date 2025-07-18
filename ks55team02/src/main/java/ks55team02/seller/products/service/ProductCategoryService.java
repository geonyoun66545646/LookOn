package ks55team02.seller.products.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile; // ⭐ import 추가

import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.seller.products.domain.ProductCategory;

public interface ProductCategoryService {
	
	/*활성*/
	void activateCategory(String categoryId);
	
	/* */
	boolean isCategoryIdExists(String categoryId);
    boolean isCategoryNameExists(String categoryName);

    /**
     * ⭐ [수정] 카테고리 정보와 이미지를 함께 수정합니다.
     * @param productCategory 수정할 카테고리 정보
     * @param categoryImageFile 새로 업로드된 이미지 파일
     * @param deleteCategoryImage 기존 이미지 삭제 여부
     */
    void updateCategory(ProductCategory productCategory, MultipartFile categoryImageFile, boolean deleteCategoryImage);
	
	/* */
	void deactivateCategoryAndRelatedProducts(String categoryId);
	
	Map<String, Object> getCategoryList(SearchCriteria searchCriteria);
	
	/**
     * ⭐ [수정] 카테고리와 이미지를 함께 등록합니다.
     * @param productCategory 등록할 카테고리 정보
     * @param categoryImageFile 업로드된 이미지 파일
     */
	void addCategory(ProductCategory productCategory, MultipartFile categoryImageFile);
	
    /**
     * 카테고리 ID로 단일 카테고리 정보를 조회합니다.
     * @param categoryId 조회할 카테고리 ID
     * @return 조회된 카테고리 객체
     */
    ProductCategory getCategoryById(String categoryId);

    /**
     * 모든 상품 카테고리 목록을 조회합니다.
     * @return 모든 상품 카테고리 목록
     */
    List<ProductCategory> getAllProductCategories();

    /**
     * 카테고리 ID를 기반으로 해당 카테고리의 계층 정보를 조회합니다.
     * @param categoryId 조회할 카테고리 ID
     * @return 카테고리 계층 정보가 담긴 Map
     */
    Map<String, Object> getCategoryHierarchy(String categoryId);

    /**
     * 최상위 카테고리 이름을 받아 해당 카테고리 ID를 반환합니다.
     * @param categoryName 최상위 카테고리 이름
     * @return 조회된 카테고리 ID
     */
    String getTopLevelCategoryIdByName(String categoryName);

    /**
     * ⭐ 추가: 모든 최상위 카테고리 (1차 카테고리) 목록을 조회합니다.
     * @return 모든 최상위 카테고리 목록
     */
    List<ProductCategory> getAllTopLevelCategories();

    /**
     * ⭐ 추가: 특정 부모 카테고리 ID에 해당하는 하위 카테고리 (2차 카테고리) 목록을 조회합니다.
     * @param parentCategoryId 부모 카테고리 ID
     * @return 하위 카테고리 목록
     */
    List<ProductCategory> getSubCategoriesByParentId(String parentCategoryId);
}