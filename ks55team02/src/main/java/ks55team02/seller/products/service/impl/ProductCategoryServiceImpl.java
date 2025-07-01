package ks55team02.seller.products.service.impl;

import ks55team02.seller.products.domain.ProductCategory;
import ks55team02.seller.products.mapper.ProductCategoryMapper;
import ks55team02.seller.products.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryMapper productCategoryMapper;

    @Override
    public List<ProductCategory> getAllProductCategories() {
        return productCategoryMapper.getAllProductCategories();
    }

    @Override
    public List<ProductCategory> getPrimaryProductCategories() {
        return productCategoryMapper.getPrimaryProductCategories();
    }

    @Override
    public List<ProductCategory> getProductCategoriesByParentId(String parentId) {
        return productCategoryMapper.getProductCategoriesByParentId(parentId);
    }
}