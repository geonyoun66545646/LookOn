package ks55team02.seller.products.service.impl;

import ks55team02.seller.products.domain.ProductCategory;
import ks55team02.seller.products.mapper.ProductCategoryMapper;
import ks55team02.seller.products.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryMapper productCategoryMapper;
    
    @Override
    public ProductCategory getCategoryById(String categoryId) {
        return productCategoryMapper.getCategoryById(categoryId);
    }
    
    @Override
    public List<ProductCategory> getAllProductCategories() {
        return productCategoryMapper.getAllProductCategories();
    }

    @Override
    public Map<String, Object> getCategoryHierarchy(String categoryId) {
        Map<String, Object> result = new HashMap<>();
        ProductCategory currentCategory = productCategoryMapper.getCategoryById(categoryId);
        result.put("currentCategory", currentCategory);

        if (currentCategory != null && currentCategory.getParentCategoryId() != null) {
            ProductCategory parentCategory = productCategoryMapper.getCategoryById(currentCategory.getParentCategoryId());
            if (parentCategory != null) {
                result.put("parentCategoryName", parentCategory.getCategoryName());
                result.put("parentCategoryId", parentCategory.getCategoryId());
                result.put("subCategories", productCategoryMapper.getSubCategoriesByParentId(parentCategory.getCategoryId()));
            }
        } else if (currentCategory != null) {
            result.put("parentCategoryName", currentCategory.getCategoryName());
            result.put("parentCategoryId", currentCategory.getCategoryId());
            result.put("subCategories", productCategoryMapper.getSubCategoriesByParentId(currentCategory.getCategoryId()));
        } else {
            result.put("parentCategoryName", "전체 상품");
            result.put("parentCategoryId", null);
            result.put("subCategories", productCategoryMapper.getAllTopLevelCategories());
        }
        return result;
    }

    @Override
    public String getTopLevelCategoryIdByName(String categoryName) {
        return productCategoryMapper.getTopLevelCategoryIdByName(categoryName);
    }
    
    // ⭐ 추가된 메소드 구현
    @Override
    public List<ProductCategory> getAllTopLevelCategories() {
        return productCategoryMapper.getAllTopLevelCategories();
    }
    
    // ⭐ 추가된 메소드 구현
    @Override
    public List<ProductCategory> getSubCategoriesByParentId(String parentCategoryId) {
        return productCategoryMapper.getSubCategoriesByParentId(parentCategoryId);
    }
}