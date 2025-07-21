package ks55team02.customer.reports.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import ks55team02.customer.reports.domain.Reports;
import ks55team02.customer.reports.domain.ReportsReasons;
import ks55team02.util.CustomerPagination;

public interface ReportsService {

	// 활성화된 신고 사유 목록 조회
	List<ReportsReasons> getActiveReportReasonList(String targetType);

	// 신고 접수 처리
	void addReport(Reports report, List<MultipartFile> evidenceFiles) throws IOException;

	// 신고 대상 유형 목록 조회
	List<String> getReportTargetTypeList();

	/**
	 * [수정] 특정 사용자의 신고 내역 목록을 '검색' 및 '페이지네이션'하여 조회
	 * 
	 * @param userNo        신고 내역을 조회할 사용자의 고유 번호
	 * @param searchKeyword 검색어
	 * @param currentPage   현재 페이지 번호
	 * @return 페이징 처리된 신고 내역 데이터 (CustomerPagination 객체)
	 */
	CustomerPagination<Map<String, Object>> getMyReportList(String userNo, String searchKeyword, int currentPage);

	/**
	 * 특정 신고 1건의 상세 정보를 조회
	 * 
	 * @param dclrId 조회할 신고의 고유 ID
	 * @return 신고 상세 정보 (첨부파일 목록 포함)
	 */
	Map<String, Object> getReportDetail(String dclrId);

}