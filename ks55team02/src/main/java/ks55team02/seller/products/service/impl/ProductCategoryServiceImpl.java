package ks55team02.seller.products.service.impl; // <<--- .impl을 추가하여 수정

import ks55team02.seller.products.domain.ProductCategory;
import ks55team02.seller.products.mapper.ProductCategoryMapper;
import ks55team02.seller.products.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service // 스프링 서비스 빈으로 등록
@RequiredArgsConstructor // Lombok으로 final 필드 자동 주입
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryMapper productCategoryMapper; // ProductCategoryMapper 주입

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