package ks55team02.admin.adminpage.inquiryadmin.reports.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReportSearch;
import ks55team02.admin.adminpage.inquiryadmin.reports.service.AdminReportService;
import lombok.RequiredArgsConstructor;

/**
 * 신고 관리 비동기 요청 처리를 위한 API 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportsApiController {

    private final AdminReportService adminReportService;

    /**
     * 신고 목록 데이터를 JSON 형태로 조회
     * @param searchCriteria 검색 및 페이징 조건
     * @return Map 형태의 JSON 데이터 (reportList, pagination 포함)
     */
    @GetMapping
    public Map<String, Object> getReportList(@ModelAttribute AdminReportSearch searchCriteria) {
        // 기존 서비스 로직을 그대로 재사용하여 데이터만 반환합니다.
        return adminReportService.getAdminReportList(searchCriteria);
    }
}