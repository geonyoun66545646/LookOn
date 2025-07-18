package ks55team02.customer.reports.service;

import java.util.List;
import java.util.Map;

public interface ReportsValueSearchService {
    List<Map<String, Object>> searchPosts(String keyword);
    List<Map<String, Object>> searchComments(String keyword);
    List<Map<String, Object>> searchProducts(String keyword);
    List<Map<String, Object>> searchUsers(String keyword);
}