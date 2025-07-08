package ks55team02.seller.products.service.impl;

import ks55team02.seller.products.domain.Products;
import ks55team02.seller.products.mapper.ProductSearchMapper;
import ks55team02.seller.products.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductSearchMapper productSearchMapper;

    @Override
    public List<Products> getAllActiveProductsForCustomer() {
        return productSearchMapper.getAllActiveProductsForCustomer();
    }

    @Override
    public Map<String, Object> getFilteredAndSortedProducts(String categoryId, String sortBy, Map<String, Object> filterParams, int currentPage) {
        Map<String, Object> paramMap = new HashMap<>(filterParams);
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
}