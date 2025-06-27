package ks55team02.seller.products.service;

import ks55team02.seller.products.domain.ProductCategory;
import java.util.List;

public interface ProductCategoryService {
    List<ProductCategory> getAllProductCategories();
    List<ProductCategory> getPrimaryProductCategories();
    // ⭐ 이 부분을 Long -> String으로 변경합니다. ⭐
    List<ProductCategory> getProductCategoriesByParentId(String parentCategoryId); // 또는 primaryCategoryId
}