package ks55team02.customer.reports.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReportsValueSearchMapper {
    List<Map<String, Object>> findPostsForReport(String keyword);
    List<Map<String, Object>> findCommentsForReport(String keyword);
    List<Map<String, Object>> findProductsForReport(String keyword);
    List<Map<String, Object>> findUsersForReport(String keyword);
}