package ks55team02.admin.adminpage.inquiryadmin.reports.service;

import java.util.Map;

import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReportDetail;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReportSearch;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.ReportProcessRequest;

public interface AdminReportsService {

	/**
     * 신고 처리 비즈니스 로직을 수행하는 메소드
     * @param request 신고 처리 요청 정보를 담은 DTO
     */
    void processReport(ReportProcessRequest request);
	
    /**
     * 관리자용 신고 목록과 페이지네이션 정보를 함께 조회
     * @param searchCriteria 검색 조건 (currentPage, pageSize, searchKey, searchValue...)
     * @return Map<String, Object> - "reportList"와 "pagination" 객체가 담긴 맵
     */
	Map<String, Object> getAdminReportList(AdminReportSearch searchCriteria);
	
	/**
	 * 특정 신고의 상세 정보와 처리 이력 목록을 함께 조회하는 서비스
	 * @param dclrId 조회할 신고의 PK
	 * @return AdminReportDetail (처리 이력이 포함된 상세 정보 DTO)
	 */
	AdminReportDetail getAdminReportDetail(String dclrId);
	
	/**
	 * [신규 추가] 처리 완료된 신고 내용을 수정하는 메소드
	 * @param request 수정할 내용을 담은 요청 객체
	 */
	void updateProcessedReport(ReportProcessRequest request);

}