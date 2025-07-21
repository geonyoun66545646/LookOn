package ks55team02.customer.reports.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ks55team02.customer.reports.mapper.ReportsValueSearchMapper;
import ks55team02.customer.reports.service.ReportsValueSearchService;

@Service
public class ReportsValueSearchServiceImpl implements ReportsValueSearchService {

    private final ReportsValueSearchMapper reportsValueSearchMapper;

    public ReportsValueSearchServiceImpl(ReportsValueSearchMapper reportsValueSearchMapper) {
        this.reportsValueSearchMapper = reportsValueSearchMapper;
    }

    @Override
    public List<Map<String, Object>> searchPosts(String keyword) {
        return reportsValueSearchMapper.findPostsForReport(keyword);
    }

    @Override
    public List<Map<String, Object>> searchComments(String keyword) {
        return reportsValueSearchMapper.findCommentsForReport(keyword);
    }

    @Override
    public List<Map<String, Object>> searchProducts(String keyword) {
        return reportsValueSearchMapper.findProductsForReport(keyword);
    }

    @Override
    public List<Map<String, Object>> searchUsers(String keyword) {
        return reportsValueSearchMapper.findUsersForReport(keyword);
    }
}