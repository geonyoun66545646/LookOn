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

	// [수정] 이 메소드 전체를 아래 코드로 교체해주세요.
	@Override
	@Transactional
	public void updateProcessedReport(ReportProcessRequest request) {
		log.info("신고 수정 로직 시작. 신고 ID: {}", request.getDclrId());

		AdminReportDetail report = adminReportsMapper.getAdminReportDetailById(request.getDclrId());
		if (report == null) {
			throw new IllegalArgumentException("수정하려는 신고가 존재하지 않습니다. ID: " + request.getDclrId());
		}

		// 1. [핵심] 가장 최근의 처리 이력을 가져와서, 이전 제재를 먼저 '해제'합니다.
		List<AdminReportHistory> historyList = adminReportsMapper.getAdminReportHistoryListById(request.getDclrId());
		if (historyList != null && !historyList.isEmpty()) {
			AdminReportHistory previousHistory = historyList.get(0); // 가장 최근 이력
			reversePreviousAction(previousHistory, report);
		}

		// 2. 기존 제재 기록을 DB에서 무효화(종료) 처리합니다.
		log.info("기존 제재 기록을 무효화합니다. 신고 ID: {}", request.getDclrId());
		adminReportsMapper.expirePreviousSanctionByReportId(request.getDclrId());

		// 3. '신규 처리' 로직을 재사용하여 새로운 제재/처리 내용을 적용합니다.
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

		// 4. 새로운 '수정' 이력을 history 테이블에 기록하고, reports 테이블의 최종 상태를 업데이트합니다.
		String nextHistoryId = adminReportsMapper.getNextReportHistoryId();
		request.setHstryId(nextHistoryId);
		request.setPrcsCmptnDt(LocalDateTime.now());

		log.info("수정된 신고 상태를 업데이트하고, 새로운 이력을 추가합니다.");
		adminReportsMapper.updateReportStatus(request);
		adminReportsMapper.insertReportHistory(request);
	}

	/**
	 * [최종 수정] 이전 처리 이력을 기반으로, 적용되었던 모든 조치를 원상 복구하는 헬퍼 메소드
	 * 
	 * @param previousHistory 가장 최근의 처리 이력
	 * @param report          신고 상세 정보 (피신고자 및 대상 콘텐츠 정보를 얻기 위함)
	 */
	private void reversePreviousAction(AdminReportHistory previousHistory, AdminReportDetail report) {
		String previousActionType = previousHistory.getActionType();
		log.info("이전 조치({})에 대한 해제/복원 로직을 실행합니다.", previousActionType);

		// --- 1. 사용자 제재 해제 로직 (기존과 동일) ---
		String userToRelease = null;
		if ("USER".equals(report.getDclrTrgtTypeCd())) {
			userToRelease = report.getDclrTrgtUserNo();
		} else {
			userToRelease = adminReportsMapper.findUserNoBySanctionedReportId(report.getDclrId());
		}

		if (userToRelease != null && !userToRelease.isEmpty()) {
			switch (previousActionType) {
			case "SUSPENSION_3D":
			case "SUSPENSION_7D":
			case "SUSPENSION_14D":
			case "SUSPENSION_30D":
			case "BAN_PERMANENT":
			case "AUTHOR_RESTRICTION":
				log.info("사용자 {}의 상태를 '활동'으로 복구합니다.", userToRelease);
				adminReportsMapper.updateUserStatus(userToRelease, "활동");
				break;
			}
		} else {
			log.warn("제재 해제 대상 사용자를 특정할 수 없습니다. 신고 ID: {}", report.getDclrId());
		}

		// --- 2. [핵심 추가] 콘텐츠 복원 로직 ---
		// 이전 조치가 '콘텐츠 삭제'였다면, 해당 콘텐츠를 복원합니다.
		if ("CONTENT_DELETION".equals(previousActionType)) {
			String targetType = report.getDclrTrgtTypeCd();
			String contentId = report.getDclrTrgtContsId();
			log.info("{} 타입의 콘텐츠(ID: {})를 복원합니다.", targetType, contentId);

			switch (targetType) {
			case "POST":
				adminReportsMapper.restorePost(contentId);
				break;
			case "COMMENT":
				adminReportsMapper.restoreComment(contentId);
				break;
			case "PRODUCT":
				adminReportsMapper.restoreProduct(contentId);
				break;
			}
		}
	}

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
		String sanctionType = request.getSanctionType();

		// [수정] if 조건문을 sanctionType != null 로 변경하여 'NONE'일 때도 실행되도록 합니다.
		if (sanctionType != null) {
			String targetUserNo = report.getDclrTrgtUserNo();
			if (targetUserNo != null && !targetUserNo.isEmpty()) {
				// [수정] '제재 없음'이 아닐 경우에만 사용자 상태를 '제한'으로 변경합니다.
				if (!"NONE".equals(sanctionType)) {
					adminReportsMapper.updateUserStatus(targetUserNo, "제한");
				}
				handleSanctionForContentOwner(targetUserNo, sanctionType, report.getDclrId(), request.getActnCn(),
						request.getAdminId(), request.getSanctionDuration());
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

				log.info("'작성자 제한'에 따른 게시글 {} 동시 삭제 조치를 실행합니다.", targetPostSn);
				adminReportsMapper.updatePostStatus(targetPostSn, "DELETED");

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

				// ▼▼▼ [핵심 추가] 바로 여기에 댓글 삭제 로직을 추가합니다. ▼▼▼
				log.info("'작성자 제한'에 따른 댓글 {} 동시 삭제 조치를 실행합니다.", targetCommentSn);
				Map<String, Object> authorRestrictionCommentDeleteParams = new HashMap<>();
				authorRestrictionCommentDeleteParams.put("commentSn", targetCommentSn);
				authorRestrictionCommentDeleteParams.put("adminId", request.getAdminId());
				adminReportsMapper.deleteComment(authorRestrictionCommentDeleteParams);
				// ▲▲▲ [핵심 추가] ▲▲▲

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
			// 1. 쿼리에 전달할 파라미터를 담을 Map을 만듭니다.
			Map<String, Object> params = new HashMap<>();

			// 2. Map에 상품번호(gdsNo)와 관리자 ID(adminId)를 넣습니다.
			params.put("gdsNo", targetGdsNo);
			params.put("adminId", request.getAdminId()); // request 객체에서 관리자 ID 가져오기

			// 3. Map을 파라미터로 넘겨 수정된 매퍼 메소드를 호출합니다.
			adminReportsMapper.updateProductExposureYn(params);

			log.info("상품 {} 비활성화(삭제) 조치 완료. 처리자: {}", targetGdsNo, request.getAdminId());
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

		// [수정] '제재 없음'이 아닐 경우에만 endDate를 계산합니다.
		if (!"NONE".equals(sanctionType)) {
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
				/* 영구 정지는 endDate가 null */ break;
			case "AUTHOR_RESTRICTION":
				if (sanctionDuration != null) {
					if (sanctionDuration.equals("3일"))
						endDate = startDate.plusDays(3);
					else if (sanctionDuration.equals("7일"))
						endDate = startDate.plusDays(7);
					else if (sanctionDuration.equals("14일"))
						endDate = startDate.plusDays(14);
					else if (sanctionDuration.equals("30일"))
						endDate = startDate.plusDays(30);
				}
				break;
			}
		}

		String nextSanctionId = adminReportsMapper.getNextSanctionId();
		UserSanction sanction = new UserSanction();
		sanction.setSanctionId(nextSanctionId);
		sanction.setUserNo(userNo);
		sanction.setDclrId(dclrId);
		sanction.setSanctionType(sanctionType); // 'NONE'도 여기에 기록됩니다.

		// [수정] sanctionType에 따라 다른 사유를 기록합니다.
		String reason = "NONE".equals(sanctionType) ? "신고 처리 수정 결과: 제재 없음" : "신고 처리 결과에 따른 계정 제한: " + prcsCn;
		sanction.setSanctionReason(reason);

		sanction.setSanctionStartDt(startDate);
		sanction.setSanctionEndDt(endDate);
		sanction.setPrcsAdminNo(adminId);

		adminReportsMapper.insertSanction(sanction);
		log.info("사용자 {}에 대한 조치 기록 (sanctionId: {}, type: {})을 user_sanctions에 저장 완료.", userNo, nextSanctionId,
				sanctionType);
	}

	// =================================================================================
	// 아래는 기존 목록/상세 조회 메소드 (수정 없음)
	// =================================================================================

	// AdminReportsServiceImpl.java 파일 내부

	// AdminReportsServiceImpl.java (이전에 제안했던 코드)
	@Override
	public Map<String, Object> getAdminReportList(AdminReportSearch searchCriteria) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("statusList", searchCriteria.getStatusList());
		paramMap.put("startDate", searchCriteria.getStartDate()); // 여기에 날짜 필드를 담았었죠.
		paramMap.put("endDate", searchCriteria.getEndDate()); // 여기에 날짜 필드를 담았었죠.
		paramMap.put("searchKey", searchCriteria.getSearchKey());
		paramMap.put("searchValue", searchCriteria.getSearchValue());

		int totalReportCount = adminReportsMapper.getAdminTotalReportCount(paramMap);

		// Pagination 객체 생성 (이 부분은 날짜 검색과 직접적인 연관은 적음)
		Pagination pagination = new Pagination(totalReportCount, searchCriteria);

		paramMap.put("pagination", pagination);

		// 이 paramMap을 가지고 매퍼를 호출합니다.
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