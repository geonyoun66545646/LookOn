package ks55team02.seller.products.service;

import ks55team02.seller.products.domain.Products;
import java.util.List;
import java.util.Map;

public interface ProductSearchService {
    List<Products> getAllActiveProductsForCustomer();
    Map<String, Object> getFilteredAndSortedProducts(String categoryId, String sortBy, Map<String, Object> filterParams, int currentPage);
}