package ks55team02.seller.products.mapper;

import ks55team02.seller.products.domain.ProductCategory; 
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper // MyBatis 매퍼임을 나타내는 어노테이션
public interface ProductCategoryMapper { 
    // 모든 카테고리 목록을 조회하는 메서드
    List<ProductCategory> getAllProductCategories();

    // 특정 부모 카테고리 ID에 해당하는 하위 카테고리 목록을 조회하는 메서드
    List<ProductCategory> getProductCategoriesByParentId(String parentId);

    // 1차 카테고리 (category_level이 1인) 목록을 조회하는 메서드
    List<ProductCategory> getPrimaryProductCategories();
}