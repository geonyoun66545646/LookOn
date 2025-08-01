package ks55team02.seller.products.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import ks55team02.seller.products.domain.ProductCategory;
import ks55team02.seller.products.domain.Products;
import ks55team02.seller.products.mapper.ProductSearchMapper;
import ks55team02.seller.products.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductSearchMapper productSearchMapper;
    
    @Override
    public List<ProductCategory> getSubCategoriesWithProductsByBrand(String upCtgryNo, String storeId) {
        return productSearchMapper.getSubCategoriesWithProductsByBrand(upCtgryNo, storeId);
    }
    
    @Override
    public List<ProductCategory> getTopLevelCategoriesByStoreId(String storeId) {
        return productSearchMapper.getTopLevelCategoriesByStoreId(storeId);
    }
    
    @Override
    public List<ProductCategory> getCategoriesByStoreId(String storeId) {
        return productSearchMapper.getCategoriesByStoreId(storeId);
    }
    
    @Override
    public List<Products> getRecentProductsByStoreId(String storeId, int limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("storeId", storeId);
        params.put("limit", limit);
        return productSearchMapper.getRecentProductsByStoreId(params);
    }
    
    @Override // ⭐ 이 부분 추가
    public List<Products> getSpecialSaleProducts(double minDiscountRate) {
        Map<String, Object> params = new HashMap<>();
        params.put("minDiscountRate", minDiscountRate);
        return productSearchMapper.getSpecialSaleProducts(params);
    }
    
    @Override
    public List<Products> getWeeklyBestProducts() {
        // 단순히 매퍼를 호출하여 DB 조회를 위임합니다.
        return productSearchMapper.getWeeklyBestProducts();
    }
    
    @Override
    public List<Products> getSimilarProducts(String categoryId, String currentGdsNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("categoryId", categoryId);
        params.put("currentGdsNo", currentGdsNo);
        return productSearchMapper.getSimilarProducts(params);
    }
    
    @Override
    public List<Products> getRecentProductsForMain(int limit) {
        return productSearchMapper.getRecentProductsForMain(limit);
    }
    
    @Override
    public List<Products> getSaleProducts() {
        return productSearchMapper.getSaleProducts();
    }

    @Override
    public List<Products> getNewProducts() {
        return productSearchMapper.getNewProducts();
    }
    
    @Override
    public List<Products> getAllActiveProductsForCustomer() {
        return productSearchMapper.getAllActiveProductsForCustomer();
    }

    @Override
    public Map<String, Object> getFilteredAndSortedProducts(String categoryId, String sortBy, Map<String, Object> filterParams, int currentPage) {
    	log.info(">>>> 전달받은 sortBy 값: {}", sortBy); // ⭐ 이 줄을 추가합니다.
        // ⭐⭐ 핵심 수정: filterParams를 그대로 복사하여 모든 필터 조건을 유지합니다. ⭐⭐
        Map<String, Object> paramMap = new HashMap<>(filterParams);
        
        // categoryId와 sortBy는 매개변수로 직접 받으므로, 맵에 다시 한번 설정해줍니다.
        paramMap.put("categoryId", categoryId);
        paramMap.put("sortBy", sortBy);
        
        // 페이지네이션 로직
        int pageSize = 12;
        int offset = (currentPage - 1) * pageSize;
        paramMap.put("pageSize", pageSize);
        paramMap.put("offset", offset);
        
        // 이후 로직은 모두 동일합니다.
        long totalProductsCount = productSearchMapper.countFilteredProducts(paramMap);
        List<Products> productsList = productSearchMapper.getFilteredAndSortedProducts(paramMap);
        int lastPage = (int) Math.ceil((double) totalProductsCount / pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("productsList", productsList);
        result.put("totalProductsCount", totalProductsCount);
        result.put("currentPage", currentPage);
        result.put("lastPage", lastPage == 0 ? 1 : lastPage);
        
        return result;
    }
    
    private Map<String, Object> processFilterParams(Map<String, Object> filterParams) {
        Map<String, Object> processedParams = new HashMap<>();

        if (filterParams != null) {
            filterParams.forEach((key, value) -> {
                if (!ObjectUtils.isEmpty(value)) {
                    processedParams.put(key, value);
                }
            });
        }

        if (processedParams.containsKey("discountRates")) {
            Object discountRateObj = processedParams.get("discountRates");
            if (discountRateObj instanceof List && !((List<?>) discountRateObj).isEmpty()) {
                String rate = String.valueOf(((List<?>) discountRateObj).get(0));
                if (!"all".equalsIgnoreCase(rate)) {
                    processedParams.put("discountRate", rate);
                }
            }
            processedParams.remove("discountRates"); // 원래 키는 제거하여 혼동 방지
        }
        return processedParams;
    }
}