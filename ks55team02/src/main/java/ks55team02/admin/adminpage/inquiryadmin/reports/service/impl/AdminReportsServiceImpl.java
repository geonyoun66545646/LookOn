package ks55team02.admin.adminpage.inquiryadmin.reports.service.impl;

// [수정] 필요한 import 구문들을 모두 포함했습니다.
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// import java.util.Optional; // UserMapper를 사용하지 않으므로 주석 처리

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReport;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReportDetail;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReportHistory;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReportSearch;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.ReportProcessRequest;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.UserSanction;
import ks55team02.admin.adminpage.inquiryadmin.reports.mapper.AdminReportsMapper;
import ks55team02.admin.adminpage.inquiryadmin.reports.service.AdminReportsService;
import ks55team02.admin.common.domain.Pagination;
// import ks55team02.admin.user.mapper.UserMapper; // UserMapper를 사용하지 않으므로 주석 처리
// import ks55team02.user.domain.User; // User를 사용하지 않으므로 주석 처리
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminReportsServiceImpl implements AdminReportsService {

	private final AdminReportsMapper adminReportsMapper;
	// [수정] UserMapper 의존성 주입을 제거합니다.
	// private final UserMapper userMapper;

	@Override
	@Transactional
	public void processReport(ReportProcessRequest request) {

		// 1. 처리할 신고의 전체 정보를 먼저 조회합니다.
		AdminReportDetail report = adminReportsMapper.getAdminReportDetailById(request.getDclrId());
		if (report == null) {
			throw new IllegalArgumentException("존재하지 않는 신고입니다. ID: " + request.getDclrId());
		}

		// 2. [추가] 중복 처리 방지 유효성 검사
		String currentStatus = report.getPrcsSttsCd();
		if ("COMPLETED".equals(currentStatus) || "REJECTED".equals(currentStatus)) {
			throw new IllegalStateException("이미 처리(" + currentStatus + ")가 완료된 신고는 다시 처리할 수 없습니다.");
		}

		/*
		 * // 3. [수정] 신고 대상 사용자 상태 확인 로직은 UserMapper가 없으므로 우선 주석 처리합니다. (TODO) String
		 * targetUserNo = report.getDclrTrgtUserNo(); String targetType =
		 * report.getDclrTrgtTypeCd();
		 * 
		 * if ("USER".equals(targetType) && targetUserNo != null &&
		 * !targetUserNo.isEmpty()) { Optional<User> targetUserOpt =
		 * userMapper.findByUserNo(targetUserNo); if (targetUserOpt.isEmpty() ||
		 * "탈퇴".equals(targetUserOpt.get().getUserStatus())) { throw new
		 * IllegalStateException("신고 대상 사용자가 존재하지 않거나 이미 탈퇴한 상태입니다."); } }
		 */

		// 4. '교통정리 경찰': 신고 대상 유형에 따라 다른 처리를 호출합니다.
		String targetType = report.getDclrTrgtTypeCd();
		switch (targetType) {
		case "USER":
			processUserSanction(request, report);
			break;
		case "POST":
			processPostAction(request, report);
			break;
		case "COMMENT":
			processCommentAction(request, report);
			break;
		}

		// 5. 공통 로직: 신고 상태를 업데이트하고, 처리 이력을 남깁니다.
		String nextHistoryId = adminReportsMapper.getNextReportHistoryId();
		request.setHstryId(nextHistoryId);

		adminReportsMapper.updateReportStatus(request);
		adminReportsMapper.insertReportHistory(request);
	}

	/**
	 * 사용자 제재를 처리하는 private 메소드
	 */
	private void processUserSanction(ReportProcessRequest request, AdminReportDetail report) {
		log.info("사용자 제재 로직 실행: {}", report.getDclrTrgtUserNo());
		String sanctionType = request.getSanctionType();

		if (sanctionType != null && !"NONE".equals(sanctionType)) {
			String targetUserNo = report.getDclrTrgtUserNo();
			adminReportsMapper.updateUserStatus(targetUserNo, "제한");

			LocalDateTime startDate = LocalDateTime.now();
			LocalDateTime endDate = null;
			if (sanctionType.contains("3일"))
				endDate = startDate.plusDays(3);
			else if (sanctionType.contains("7일"))
				endDate = startDate.plusDays(7);
			else if (sanctionType.contains("14일"))
				endDate = startDate.plusDays(14);
			else if (sanctionType.contains("30일"))
				endDate = startDate.plusDays(30);

			UserSanction sanction = new UserSanction();
			sanction.setUserNo(targetUserNo);
			sanction.setDclrId(request.getDclrId());
			sanction.setSanctionType(sanctionType);
			sanction.setSanctionReason("신고 처리 결과에 따른 제재: " + request.getPrcsCn());
			sanction.setSanctionStartDt(startDate);
			sanction.setSanctionEndDt(endDate);
			sanction.setPrcsAdminNo(request.getAdminId());

			adminReportsMapper.insertSanction(sanction);
		}
	}

	/**
	 * 게시글 관련 조치를 처리하는 private 메소드
	 */
	private void processPostAction(ReportProcessRequest request, AdminReportDetail report) {
		log.info("게시글 조치 로직 실행: {}", report.getDclrTrgtContsId());
		// TODO: 게시글 Mapper를 주입받아, 해당 게시글의 상태를 '숨김' 또는 '삭제'로 변경하는 로직 구현
	}

	/**
	 * 댓글 관련 조치를 처리하는 private 메소드
	 */
	private void processCommentAction(ReportProcessRequest request, AdminReportDetail report) {
		log.info("댓글 조치 로직 실행: {}", report.getDclrTrgtContsId());
		// TODO: 댓글 Mapper를 주입받아, 해당 댓글을 삭제하는 로직 구현
	}

	// --- [유지] 파트너님의 기존 목록 조회 메소드 ---
	@Override
	public Map<String, Object> getAdminReportList(AdminReportSearch searchCriteria) {
		int totalReportCount = adminReportsMapper.getAdminTotalReportCount(searchCriteria);
		Pagination pagination = new Pagination(totalReportCount, searchCriteria);
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("pagination", pagination);
		paramMap.put("searchCriteria", searchCriteria);
		List<AdminReport> reportList = adminReportsMapper.getAdminReportList(paramMap);
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("reportList", reportList);
		resultMap.put("pagination", pagination);
		return resultMap;
	}

	// --- [유지] 파트너님의 기존 상세 조회 메소드 ---
	@Override
	public AdminReportDetail getAdminReportDetail(String dclrId) {
		AdminReportDetail reportDetail = adminReportsMapper.getAdminReportDetailById(dclrId);
		if (reportDetail != null) {
			List<AdminReportHistory> historyList = adminReportsMapper.getAdminReportHistoryListById(dclrId);
			reportDetail.setHistoryList(historyList);
		}
		return reportDetail;
	}
}