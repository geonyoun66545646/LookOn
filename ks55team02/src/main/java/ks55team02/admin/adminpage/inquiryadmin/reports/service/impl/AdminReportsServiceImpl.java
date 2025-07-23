package ks55team02.admin.adminpage.inquiryadmin.reports.service.impl;

import java.time.LocalDateTime;
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
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.UserSanction;
import ks55team02.admin.adminpage.inquiryadmin.reports.mapper.AdminReportsMapper;
import ks55team02.admin.adminpage.inquiryadmin.reports.service.AdminReportsService;
import ks55team02.admin.common.domain.Pagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminReportsServiceImpl implements AdminReportsService {

	private final AdminReportsMapper adminReportsMapper;

	@Override
	@Transactional
	public void processReport(ReportProcessRequest request) {
		AdminReportDetail report = adminReportsMapper.getAdminReportDetailById(request.getDclrId());
		if (report == null) {
			throw new IllegalArgumentException("존재하지 않는 신고입니다. ID: " + request.getDclrId());
		}
		String currentStatus = report.getPrcsSttsCd();
		if ("COMPLETED".equals(currentStatus) || "REJECTED".equals(currentStatus)) {
			throw new IllegalStateException("이미 처리(" + currentStatus + ")가 완료된 신고는 다시 처리할 수 없습니다.");
		}

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
		case "PRODUCT":
			processProductAction(request, report);
			break;
		}

		String nextHistoryId = adminReportsMapper.getNextReportHistoryId();
		request.setHstryId(nextHistoryId);

		String newStatus = request.getNewStatus();
		if ("COMPLETED".equals(newStatus) || "REJECTED".equals(newStatus)) {
			request.setPrcsCmptnDt(LocalDateTime.now());
		}
		adminReportsMapper.updateReportStatus(request);
		adminReportsMapper.insertReportHistory(request);
	}

	private void processUserSanction(ReportProcessRequest request, AdminReportDetail report) {
		log.info("========== processUserSanction 메소드 시작 ==========");
		// [코드 기반으로 수정] sanctionType에는 이제 "SUSPENSION_3D"와 같은 영문 코드가 들어옵니다.
		String sanctionType = request.getSanctionType();
		if (sanctionType != null && !"NONE".equals(sanctionType)) {
			String targetUserNo = report.getDclrTrgtUserNo();
			if (targetUserNo != null && !targetUserNo.isEmpty()) {
				adminReportsMapper.updateUserStatus(targetUserNo, "제한");
				handleSanctionForContentOwner(targetUserNo, sanctionType, report.getDclrId(), request.getActnCn(),
						request.getAdminId(), request.getSanctionDuration()); // sanctionDuration 전달
			} else {
				log.warn("사용자 제재 대상 userNo가 없거나 비어있어 제재 로직을 건너뜁니다. 신고ID: {}", request.getDclrId());
			}
		}
		log.info("========== processUserSanction 메소드 종료 ==========");
	}

	private void processPostAction(ReportProcessRequest request, AdminReportDetail report) {
		log.info("========== processPostAction 메소드 시작 ==========");
		String actionType = request.getSanctionType();
		String targetPostSn = report.getDclrTrgtContsId();

		// [코드 기반으로 수정] switch-case 문의 case를 전부 영문 코드로 변경합니다.
		switch (actionType) {
		case "CONTENT_DELETION":
			adminReportsMapper.updatePostStatus(targetPostSn, "DELETED");
			log.info("게시글 {} 삭제 조치 완료.", targetPostSn);
			break;
		case "AUTHOR_RESTRICTION":
			String postOwnerUserNo = adminReportsMapper.getPostOwnerUserNo(targetPostSn);
			if (postOwnerUserNo != null && !postOwnerUserNo.isEmpty()) {
				adminReportsMapper.updateUserStatus(postOwnerUserNo, "제한");
				handleSanctionForContentOwner(postOwnerUserNo, actionType, report.getDclrId(), request.getActnCn(),
						request.getAdminId(), request.getSanctionDuration()); // sanctionDuration 전달
			} else {
				log.warn("게시글 작성자 userNo를 찾을 수 없어 계정 제한 조치를 할 수 없습니다. 게시글: {}", targetPostSn);
			}
			break;
		case "NONE":
		default:
			log.info("게시글 {}에 대해 '제재 없음' 또는 정의되지 않은 조치({}) 선택.", targetPostSn, actionType);
			break;
		}
		log.info("========== processPostAction 메소드 종료 ==========");
	}

	private void processCommentAction(ReportProcessRequest request, AdminReportDetail report) {
		log.info("========== processCommentAction 메소드 시작 ==========");
		String actionType = request.getSanctionType();
		String targetCommentSn = report.getDclrTrgtContsId();

		switch (actionType) {
		case "CONTENT_DELETION":
			Map<String, Object> commentDeleteParams = new HashMap<>();
			commentDeleteParams.put("commentSn", targetCommentSn);
			commentDeleteParams.put("adminId", request.getAdminId());
			adminReportsMapper.deleteComment(commentDeleteParams);
			log.info("댓글 {} 소프트 삭제 조치 완료.", targetCommentSn);
			break;
		case "AUTHOR_RESTRICTION":
			String commentOwnerUserNo = adminReportsMapper.getCommentOwnerUserNo(targetCommentSn);
			if (commentOwnerUserNo != null && !commentOwnerUserNo.isEmpty()) {
				adminReportsMapper.updateUserStatus(commentOwnerUserNo, "제한");
				handleSanctionForContentOwner(commentOwnerUserNo, actionType, report.getDclrId(), request.getActnCn(),
						request.getAdminId(), request.getSanctionDuration()); // sanctionDuration 전달
			} else {
				log.warn("댓글 작성자 userNo를 찾을 수 없어 계정 제한 조치를 할 수 없습니다. 댓글: {}", targetCommentSn);
			}
			break;
		case "NONE":
		default:
			log.info("댓글 {}에 대해 '제재 없음' 또는 정의되지 않은 조치({}) 선택.", targetCommentSn, actionType);
			break;
		}
		log.info("========== processCommentAction 메소드 종료 ==========");
	}

	private void processProductAction(ReportProcessRequest request, AdminReportDetail report) {
		log.info("========== processProductAction 메소드 시작 ==========");
		String actionType = request.getSanctionType();
		String targetGdsNo = report.getDclrTrgtContsId();

		if ("CONTENT_DELETION".equals(actionType)) {
			adminReportsMapper.updateProductExposureYn(targetGdsNo, false);
			log.info("상품 {} 삭제 조치 완료.", targetGdsNo);
		} else {
			log.info("상품 {}에 대해 '제재 없음' 또는 정의되지 않은 조치({}) 선택.", targetGdsNo, actionType);
		}
		log.info("========== processProductAction 메소드 종료 ==========");
	}

	private void handleSanctionForContentOwner(String userNo, String sanctionType, String dclrId, String prcsCn,
			String adminId, String sanctionDuration) {
		log.info("handleSanctionForContentOwner 실행: userNo={}, sanctionType={}, sanctionDuration={}", userNo,
				sanctionType, sanctionDuration);

		LocalDateTime startDate = LocalDateTime.now();
		LocalDateTime endDate = null;

		// [코드 기반으로 수정] 문자열 포함 여부(.contains) 대신, 정확한 코드로 기간을 계산합니다.
		switch (sanctionType) {
		case "SUSPENSION_3D":
			endDate = startDate.plusDays(3);
			break;
		case "SUSPENSION_7D":
			endDate = startDate.plusDays(7);
			break;
		case "SUSPENSION_14D":
			endDate = startDate.plusDays(14);
			break;
		case "SUSPENSION_30D":
			endDate = startDate.plusDays(30);
			break;
		case "BAN_PERMANENT":
			// 영구 정지는 endDate가 null이므로 아무것도 하지 않음
			break;
		case "AUTHOR_RESTRICTION":
			// '작성자 제한'의 경우, 함께 넘어온 sanctionDuration 값으로 기간을 설정합니다.
			if (sanctionDuration != null) {
				if (sanctionDuration.equals("3일"))
					endDate = startDate.plusDays(3);
				else if (sanctionDuration.equals("7일"))
					endDate = startDate.plusDays(7);
				else if (sanctionDuration.equals("14일"))
					endDate = startDate.plusDays(14);
				else if (sanctionDuration.equals("30일"))
					endDate = startDate.plusDays(30);
				// "영구"는 endDate를 null로 유지
			}
			break;
		}

		String nextSanctionId = adminReportsMapper.getNextSanctionId();
		UserSanction sanction = new UserSanction();
		sanction.setSanctionId(nextSanctionId);
		sanction.setUserNo(userNo);
		sanction.setDclrId(dclrId);
		// [코드 기반으로 수정] DB의 sanction_type 컬럼에는 이제 'SUSPENSION_3D' 같은 코드가 저장됩니다.
		sanction.setSanctionType(sanctionType);
		sanction.setSanctionReason("신고 처리 결과에 따른 계정 제한: " + prcsCn);
		sanction.setSanctionStartDt(startDate);
		sanction.setSanctionEndDt(endDate);
		sanction.setPrcsAdminNo(adminId);

		adminReportsMapper.insertSanction(sanction);
		log.info("콘텐츠 소유자 {} 계정 제한 기록 (sanctionId: {}) user_sanctions에 저장 완료.", userNo, nextSanctionId);
	}


	// =================================================================================
	// 아래는 기존 목록/상세 조회 메소드 (수정 없음)
	// =================================================================================

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