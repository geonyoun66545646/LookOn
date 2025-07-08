package ks55team02.seller.products.mapper;

import ks55team02.seller.products.domain.ProductCategory;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ProductCategoryMapper {

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