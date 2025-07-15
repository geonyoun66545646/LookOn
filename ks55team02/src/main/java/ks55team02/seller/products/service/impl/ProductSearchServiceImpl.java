package ks55team02.seller.products.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import ks55team02.seller.products.domain.Products;
import ks55team02.seller.products.mapper.ProductSearchMapper;
import ks55team02.seller.products.service.ProductSearchService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductSearchMapper productSearchMapper;
    
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
    	Map<String, Object> paramMap = processFilterParams(filterParams);
        paramMap.put("categoryId", categoryId);
        paramMap.put("sortBy", sortBy);
        
        // 페이지네이션 로직
        int pageSize = 12;
        int offset = (currentPage - 1) * pageSize;
        paramMap.put("pageSize", pageSize);
        paramMap.put("offset", offset);
        
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