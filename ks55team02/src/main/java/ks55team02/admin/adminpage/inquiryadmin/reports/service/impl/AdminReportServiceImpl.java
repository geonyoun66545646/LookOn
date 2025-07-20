package ks55team02.admin.adminpage.inquiryadmin.reports.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReport;
import ks55team02.admin.adminpage.inquiryadmin.reports.mapper.AdminReportMapper;
import ks55team02.admin.adminpage.inquiryadmin.reports.service.AdminReportService;
import ks55team02.admin.common.domain.Pagination;
import ks55team02.admin.common.domain.SearchCriteria;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {

    private final AdminReportMapper adminReportMapper;

    /**
     * 관리자용 신고 목록과 페이지네이션 정보를 함께 조회
     */
    @Override
    public Map<String, Object> getAdminReportList(SearchCriteria searchCriteria) {

        // 1. 조건에 맞는 전체 데이터 개수 조회 (from Mapper)
        int totalReportCount = adminReportMapper.getAdminTotalReportCount(searchCriteria);

        // 2. 페이지네이션 객체 생성 (우리가 받은 그 Pagination 클래스!)
        Pagination pagination = new Pagination(totalReportCount, searchCriteria);

        // 3. 페이징과 검색 조건을 담을 Map 생성
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pagination", pagination);
        paramMap.put("searchCriteria", searchCriteria);

        // 4. 페이징 처리된 신고 목록 조회 (from Mapper)
        List<AdminReport> reportList = adminReportMapper.getAdminReportList(paramMap);

        // 5. 컨트롤러에 전달할 최종 결과 Map 생성
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("reportList", reportList);
        resultMap.put("pagination", pagination);

        return resultMap;
    }
}