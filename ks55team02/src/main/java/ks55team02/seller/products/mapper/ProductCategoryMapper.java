package ks55team02.seller.products.mapper; // 실제 패키지 경로에 맞게 수정

import ks55team02.seller.products.domain.ProductCategory; // <<--- Category 대신 ProductCategory 임포트
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper // MyBatis 매퍼임을 나타내는 어노테이션
public interface ProductCategoryMapper { // <<--- CategoryMapper 대신 ProductCategoryMapper로 변경

    // 모든 카테고리 목록을 조회하는 메서드
    List<ProductCategory> getAllProductCategories(); // <<--- 메서드명도 변경

    // 특정 부모 카테고리 ID에 해당하는 하위 카테고리 목록을 조회하는 메서드
    List<ProductCategory> getProductCategoriesByParentId(String parentId); // <<--- 메서드명도 변경

    // 1차 카테고리 (category_level이 1인) 목록을 조회하는 메서드
    List<ProductCategory> getPrimaryProductCategories(); // <<--- 메서드명도 변경
}