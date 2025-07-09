package ks55team02.seller.products.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import ks55team02.admin.common.domain.Pagination;
import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.seller.products.domain.ProductCategory;
import ks55team02.seller.products.mapper.ProductCategoryMapper;
import ks55team02.seller.products.mapper.ProductsMapper;
import ks55team02.seller.products.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryMapper productCategoryMapper;
    private final ProductsMapper productsMapper;
    
    @Override
    public void activateCategory(String categoryId) {
        productCategoryMapper.activateCategory(categoryId);
    }
    
 // ProductCategoryServiceImpl.java
    @Override
    public boolean isCategoryIdExists(String categoryId) {
        return productCategoryMapper.countCategoryById(categoryId) > 0;
    }

    @Override
    public boolean isCategoryNameExists(String categoryName) {
        return productCategoryMapper.countCategoryByName(categoryName) > 0;
    }

    @Override
    public void updateCategory(ProductCategory productCategory) {
        // 상위 카테고리 ID가 빈 문자열이면 null로 변경
        if (productCategory.getParentCategoryId() != null && productCategory.getParentCategoryId().isEmpty()) {
            productCategory.setParentCategoryId(null);
        }
        // 카테고리 레벨 설정
        productCategory.setCategoryLevel(StringUtils.hasText(productCategory.getParentCategoryId()) ? 2 : 1);
        
        productCategoryMapper.updateCategory(productCategory);
    }
    
    @Override
    @Transactional
    public void deactivateCategoryAndRelatedProducts(String categoryId) {
    	// 1. 해당 카테고리에 속한 상품들을 먼저 비노출 처리
        productsMapper.deactivateProductsByCategoryId(categoryId);
        
        // 2. 카테고리 자체를 비활성화
        productCategoryMapper.deactivateCategory(categoryId);
    }
    
    @Override
    public void addCategory(ProductCategory productCategory) {
        
        // ⭐ 수정: parentCategoryId가 빈 문자열("")이면 null로 변경
        if (productCategory.getParentCategoryId() != null && productCategory.getParentCategoryId().isEmpty()) {
            productCategory.setParentCategoryId(null);
        }

        // 1. 카테고리 레벨(ctgry_dpth) 설정
        if (productCategory.getParentCategoryId() != null) { // 이제 StringUtils.hasText 대신 null 체크만 해도 됨
            productCategory.setCategoryLevel(2);
        } else {
            productCategory.setCategoryLevel(1);
        }
        
        // 2. 활성 상태 기본값 설정
        productCategory.setActivationYn(true);
        
        // 3. Mapper 호출하여 DB에 INSERT
        productCategoryMapper.addCategory(productCategory);
    }
    
    @Override
    public Map<String, Object> getCategoryList(SearchCriteria searchCriteria) {
        // 1. 검색 조건에 맞는 데이터의 총 개수 조회
        int categoryListCount = productCategoryMapper.getCategoryListCount(searchCriteria);
        
        // 2. 페이지네이션 객체 생성 (이때 모든 페이지 정보 및 offset이 계산됨)
        Pagination pagination = new Pagination(categoryListCount, searchCriteria);
        
        // 3. SearchCriteria에 계산된 offset 설정
        // searchCriteria.setOffset() 호출이 불필요해 보일 수 있으나,
        // Pagination 생성자에서 currentPage 보정이 일어날 수 있으므로,
        // Mapper에 전달하기 전에 최종 계산된 offset 값으로 동기화해주는 것이 안전합니다.
        searchCriteria.setOffset(pagination.getLimitStart());
        
        // 4. 페이지네이션이 적용된 목록 조회
        List<ProductCategory> categoryList = productCategoryMapper.getCategoryList(searchCriteria);
        
        // 5. 컨트롤러에 전달할 Map 생성
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("pagination", pagination);
        resultMap.put("categoryList", categoryList);
        
        return resultMap;
    }
    
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