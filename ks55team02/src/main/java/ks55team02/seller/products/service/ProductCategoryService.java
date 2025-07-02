package ks55team02.seller.products.service;

import ks55team02.seller.products.domain.ProductCategory;
import java.util.List;

public interface ProductCategoryService {
    List<ProductCategory> getAllProductCategories();
    List<ProductCategory> getPrimaryProductCategories();
    List<ProductCategory> getProductCategoriesByParentId(String parentCategoryId);
}