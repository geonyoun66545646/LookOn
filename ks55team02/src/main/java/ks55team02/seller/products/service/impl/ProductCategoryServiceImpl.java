package ks55team02.seller.products.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import ks55team02.admin.common.domain.Pagination;
import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.seller.products.domain.ProductCategory;
import ks55team02.seller.products.mapper.ProductCategoryMapper;
import ks55team02.seller.products.mapper.ProductsMapper;
import ks55team02.seller.products.service.ProductCategoryService;
import ks55team02.util.FileDetail;
import ks55team02.util.FilesUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional // ⭐ 클래스 레벨에 트랜잭션 추가
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryMapper productCategoryMapper;
    private final ProductsMapper productsMapper;
    private final FilesUtils filesUtils; // ⭐ FilesUtils 의존성 주입 추가
    
    @Override
    @Transactional
    public void deactivateCategoryAndRelatedProducts(String categoryId, String adminId) {
        // 1. 해당 카테고리에 속한 상품들을 먼저 비활성화
        productsMapper.deactivateProductsByCategoryId(categoryId, adminId); // ⭐ Mapper 호출 시 ID 전달
        
        // 2. 카테고리 자체를 비활성화
        productCategoryMapper.deactivateCategory(categoryId, adminId); // ⭐ Mapper 호출 시 ID 전달
    }
    
    @Override
    public void activateCategory(String categoryId) {
        productCategoryMapper.activateCategory(categoryId);
    }
    
    @Override
    public boolean isCategoryIdExists(String categoryId) {
        return productCategoryMapper.countCategoryById(categoryId) > 0;
    }

    @Override
    public boolean isCategoryNameExists(String categoryName) {
        return productCategoryMapper.countCategoryByName(categoryName) > 0;
    }

    /**
     * ⭐ [수정] 카테고리 정보와 이미지를 함께 수정합니다.
     */
    @Override
    public void updateCategory(ProductCategory productCategory, MultipartFile categoryImageFile, boolean deleteCategoryImage) {
        // 1. 기존 이미지 삭제가 체크된 경우
        if (deleteCategoryImage) {
            ProductCategory existingCategory = productCategoryMapper.getCategoryById(productCategory.getCategoryId());
            if (existingCategory != null && existingCategory.getCategoryImagePath() != null) {
                filesUtils.deleteFile(existingCategory.getCategoryImagePath()); // 물리적 파일 삭제
                productCategory.setCategoryImagePath(""); // DB 경로를 빈 문자열로 설정 (삭제 표시)
            }
        }

        // 2. 새로운 이미지 파일이 업로드된 경우
        if (categoryImageFile != null && !categoryImageFile.isEmpty()) {
            // 기존 이미지가 있었다면 먼저 삭제
            ProductCategory existingCategory = productCategoryMapper.getCategoryById(productCategory.getCategoryId());
            if (existingCategory != null && existingCategory.getCategoryImagePath() != null) {
                filesUtils.deleteFile(existingCategory.getCategoryImagePath());
            }
            
            // 새 파일 저장 및 경로 설정
            FileDetail fileDetail = filesUtils.saveFile(categoryImageFile, "category");
            if (fileDetail != null) {
                productCategory.setCategoryImagePath(fileDetail.getSavedPath());
            }
        }
        
        // 3. 상위 카테고리 ID 및 레벨 설정 (기존 로직)
        if (productCategory.getParentCategoryId() != null && productCategory.getParentCategoryId().isEmpty()) {
            productCategory.setParentCategoryId(null);
        }
        productCategory.setCategoryLevel(StringUtils.hasText(productCategory.getParentCategoryId()) ? 2 : 1);
        
        // 4. DB 업데이트
        productCategoryMapper.updateCategory(productCategory);
    }
    
    @Override
    public void deactivateCategoryAndRelatedProducts(String categoryId) {
    	productsMapper.deactivateProductsByCategoryId(categoryId);
        productCategoryMapper.deactivateCategory(categoryId);
    }
    
    /**
     * ⭐ [수정] 카테고리와 이미지를 함께 등록합니다.
     */
    @Override
    public void addCategory(ProductCategory productCategory, MultipartFile categoryImageFile) {
        // 1. 이미지 파일 처리
        if (categoryImageFile != null && !categoryImageFile.isEmpty()) {
            FileDetail fileDetail = filesUtils.saveFile(categoryImageFile, "category");
            if(fileDetail != null) {
                productCategory.setCategoryImagePath(fileDetail.getSavedPath());
            }
        }
        
        // 2. 카테고리 정보 설정 (기존 로직)
        if (productCategory.getParentCategoryId() != null && productCategory.getParentCategoryId().isEmpty()) {
            productCategory.setParentCategoryId(null);
        }
        productCategory.setCategoryLevel(StringUtils.hasText(productCategory.getParentCategoryId()) ? 2 : 1);
        productCategory.setActivationYn(true);
        
        // 3. Mapper 호출하여 DB에 INSERT
        productCategoryMapper.addCategory(productCategory);
    }
    
    @Override
    public Map<String, Object> getCategoryList(SearchCriteria searchCriteria) {
        int categoryListCount = productCategoryMapper.getCategoryListCount(searchCriteria);
        Pagination pagination = new Pagination(categoryListCount, searchCriteria);
        searchCriteria.setOffset(pagination.getLimitStart());
        
        List<ProductCategory> categoryList = productCategoryMapper.getCategoryList(searchCriteria);
        
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
    
    @Override
    public List<ProductCategory> getAllTopLevelCategories() {
        return productCategoryMapper.getAllTopLevelCategories();
    }
    
    @Override
    public List<ProductCategory> getSubCategoriesByParentId(String parentCategoryId) {
        return productCategoryMapper.getSubCategoriesByParentId(parentCategoryId);
    }
}