package ks55team02.admin.adminpage.inquiryadmin.reports.service;

import java.util.Map;
import ks55team02.admin.common.domain.SearchCriteria;

public interface AdminReportService {

    /**
     * 관리자용 신고 목록과 페이지네이션 정보를 함께 조회
     * @param searchCriteria 검색 조건 (currentPage, pageSize, searchKey, searchValue...)
     * @return Map<String, Object> - "reportList"와 "pagination" 객체가 담긴 맵
     */
    Map<String, Object> getAdminReportList(SearchCriteria searchCriteria);

}