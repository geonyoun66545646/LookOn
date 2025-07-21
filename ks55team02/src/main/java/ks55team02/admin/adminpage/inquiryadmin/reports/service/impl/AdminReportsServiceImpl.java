package ks55team02.admin.adminpage.inquiryadmin.reports.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReport;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReportDetail;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReportHistory;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReportSearch;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.ReportProcessRequest;
import ks55team02.admin.adminpage.inquiryadmin.reports.mapper.AdminReportsMapper;
import ks55team02.admin.adminpage.inquiryadmin.reports.service.AdminReportsService;
import ks55team02.admin.common.domain.Pagination;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminReportsServiceImpl implements AdminReportsService {

	private final AdminReportsMapper adminReportsMapper;

	@Override
	@Transactional
	public void processReport(ReportProcessRequest request) {

		// 1. 새로운 이력 ID를 생성합니다.
		String nextHistoryId = adminReportsMapper.getNextReportHistoryId();

		// 2. 생성된 ID를 request 객체에 담아줍니다.
		request.setHstryId(nextHistoryId);

		// 3. reports 테이블의 상태를 업데이트합니다.
		adminReportsMapper.updateReportStatus(request);

		// 4. report_history 테이블에 이력을 삽입합니다.
		adminReportsMapper.insertReportHistory(request);
	}

	/**
	 * 관리자용 신고 목록과 페이지네이션 정보를 함께 조회
	 */
	@Override
	public Map<String, Object> getAdminReportList(AdminReportSearch searchCriteria) {

		// 1. 조건에 맞는 전체 데이터 개수 조회 (from Mapper)
		int totalReportCount = adminReportsMapper.getAdminTotalReportCount(searchCriteria);

		// 2. 페이지네이션 객체 생성 (우리가 받은 그 Pagination 클래스!)
		Pagination pagination = new Pagination(totalReportCount, searchCriteria);

		// 3. 페이징과 검색 조건을 담을 Map 생성
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("pagination", pagination);
		paramMap.put("searchCriteria", searchCriteria);

		// 4. 페이징 처리된 신고 목록 조회 (from Mapper)
		List<AdminReport> reportList = adminReportsMapper.getAdminReportList(paramMap);

		// 5. 컨트롤러에 전달할 최종 결과 Map 생성
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("reportList", reportList);
		resultMap.put("pagination", pagination);

		return resultMap;
	}

	/**
	 * 신고 상세 정보 조회 로직 구현
	 * 
	 * @param dclrId
	 * @return AdminReportDetail (상세 정보 + 처리 이력)
	 */
	@Override
	public AdminReportDetail getAdminReportDetail(String dclrId) {

		// 1. Mapper를 호출하여 '신고 상세 정보(1)'를 조회합니다.
		AdminReportDetail reportDetail = adminReportsMapper.getAdminReportDetailById(dclrId);

		// reportDetail이 null이 아닐 경우에만 후속 작업을 진행합니다. (NullPointerException 방지)
		if (reportDetail != null) {

			// 2. Mapper를 호출하여 '처리 이력 목록(N)'을 조회합니다.
			List<AdminReportHistory> historyList = adminReportsMapper.getAdminReportHistoryListById(dclrId);

			// 3. 조회된 '처리 이력 목록'을 '신고 상세 정보' DTO에 주입(set)합니다.
			// 이것이 바로 1:N 관계의 데이터를 조합하는 핵심 로직입니다.
			reportDetail.setHistoryList(historyList);
		}

		// 4. 완벽하게 조합된 DTO 객체를 Controller로 반환합니다.
		return reportDetail;
	}
}