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
	// private final UserMapper userMapper; // 주석 유지

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
		 * report.getDclrTrgtTypeCd(); * if ("USER".equals(targetType) && targetUserNo
		 * != null && !targetUserNo.isEmpty()) { Optional<User> targetUserOpt =
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
			processPostAction(request, report); // [수정] 메소드 호출
			break;
		case "COMMENT":
			processCommentAction(request, report); // [수정] 메소드 호출
			break;
		case "PRODUCT": // [추가] PRODUCT 타입 케이스
			processProductAction(request, report);
			break;
		}

		// 5. 공통 로직: 신고 상태를 업데이트하고, 처리 이력을 남깁니다.
		String nextHistoryId = adminReportsMapper.getNextReportHistoryId();
		request.setHstryId(nextHistoryId);

		// ★★★★★★★★★★ 이 코드를 추가해야 합니다 ★★★★★★★★★★
		// 처리 상태가 'COMPLETED' 또는 'REJECTED'일 경우,
		// request 객체에 현재 시간을 처리 완료 일시로 설정합니다.
		// AdminReportsServiceImpl.java의 processReport 메소드 내부
		String newStatus = request.getNewStatus();
		if ("COMPLETED".equals(newStatus) || "REJECTED".equals(newStatus)) {
		    request.setPrcsCmptnDt(LocalDateTime.now());
		}
		adminReportsMapper.updateReportStatus(request);
		
		adminReportsMapper.insertReportHistory(request);
	}

	/**
	 * 사용자 제재를 처리하는 private 메소드
	 */
	private void processUserSanction(ReportProcessRequest request, AdminReportDetail report) {
		log.info("========== processUserSanction 메소드 시작 ==========");
		log.info("사용자 제재 로직 실행 대상 userNo: {}", report.getDclrTrgtUserNo());
		String sanctionType = request.getSanctionType();

		if (sanctionType != null && !"NONE".equals(sanctionType)) {
			String targetUserNo = report.getDclrTrgtUserNo();

			if (targetUserNo != null && !targetUserNo.isEmpty()) {
				// user_status를 '제한'으로 변경
				adminReportsMapper.updateUserStatus(targetUserNo, "제한");
				log.info("사용자 {}의 상태를 '제한'으로 변경 완료.", targetUserNo);

				// 제재 기간 계산
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
				log.info("제재 시작일: {}, 종료일: {}", startDate, endDate);

				// [핵심 추가] 1. 새로운 sanctionId를 생성합니다.
				String nextSanctionId = adminReportsMapper.getNextSanctionId();
				log.info("생성된 sanctionId: {}", nextSanctionId);

				// [수정] UserSanction 객체에 생성된 ID를 설정합니다.
				UserSanction sanction = new UserSanction();
				sanction.setSanctionId(nextSanctionId);
				sanction.setUserNo(targetUserNo);
				sanction.setDclrId(request.getDclrId());
				sanction.setSanctionType(sanctionType);
				sanction.setSanctionReason("신고 처리 결과에 따른 제재: " + request.getPrcsCn());
				sanction.setSanctionStartDt(startDate);
				sanction.setSanctionEndDt(endDate);
				sanction.setPrcsAdminNo(request.getAdminId());

				adminReportsMapper.insertSanction(sanction);
				log.info("========== insertSanction 호출 완료. sanctionId: {} ==========", nextSanctionId);

			} else {
				log.warn("사용자 제재 대상 userNo가 없거나 비어있어 제재 로직을 건너뜁니다. 신고ID: {}", request.getDclrId());
			}
		} else {
			log.info("사용자 제재 유형이 'NONE'이거나 null이므로 제재 로직을 실행하지 않습니다. 신고ID: {}", request.getDclrId());
		}
		log.info("========== processUserSanction 메소드 종료 ==========");
	}

	private void processPostAction(ReportProcessRequest request, AdminReportDetail report) {
		log.info("========== processPostAction 메소드 시작 =========="); // [추가] 디버그 로그
		log.info("게시글 조치 로직 실행 대상 postSn: {}", report.getDclrTrgtContsId());

		String actionType = request.getSanctionType(); // 제재 유형 필드를 조치 유형으로 재활용
		String targetPostSn = report.getDclrTrgtContsId();
		String postOwnerUserNo = null;

		if (targetPostSn != null && !targetPostSn.isEmpty()) {
			postOwnerUserNo = adminReportsMapper.getPostOwnerUserNo(targetPostSn); // 게시글 작성자 조회
		} else {
			log.warn("게시글 조치 대상 postSn이 없거나 비어있어 게시글 조치 로직을 건너뜁니다. 신고ID: {}", request.getDclrId());
			log.info("========== processPostAction 메소드 종료 ==========");
			return;
		}

		switch (actionType) {
		case "삭제 조치":
			adminReportsMapper.updatePostStatus(targetPostSn, "DELETED"); // 'DELETED'는 예시 상태 코드, 실제 DB에 맞게 수정
			log.info("게시글 {} 삭제 조치 완료. (상태 DELETED로 변경)", targetPostSn);
			break;
		case "작성자 제한":
			if (postOwnerUserNo != null && !postOwnerUserNo.isEmpty()) {
				adminReportsMapper.updateUserStatus(postOwnerUserNo, "제한");
				log.info("게시글 작성자 {} 계정 제한 조치 완료.", postOwnerUserNo);
				// user_sanctions 테이블에 제재 기록도 남겨야 합니다 (processUserSanction 로직 재활용)
				// [수정] handleSanctionForContentOwner 호출 시 request.getSanctionDuration() 인자 추가
				handleSanctionForContentOwner(postOwnerUserNo, actionType, report.getDclrId(), request.getPrcsCn(),
						request.getAdminId(), request.getSanctionDuration()); // [수정] sanctionDuration 전달
			} else {
				log.warn("게시글 작성자 userNo를 찾을 수 없어 계정 제한 조치를 할 수 없습니다. 게시글: {}", targetPostSn);
			}
			break;
		case "제재 없음":
			log.info("게시글 {}에 대해 '제재 없음' 조치 선택.", targetPostSn);
			break;
		default:
			log.warn("알 수 없는 게시글 조치 유형: {}. 신고ID: {}", actionType, request.getDclrId());
			break;
		}
		log.info("========== processPostAction 메소드 종료 =========="); // [추가] 디버그 로그
	}

	/**
	 * [구현] 댓글 관련 조치를 처리하는 private 메소드 조치 종류: 삭제 조치 / 작성자 제한 / 제재 없음
	 */
	private void processCommentAction(ReportProcessRequest request, AdminReportDetail report) {
		log.info("========== processCommentAction 메소드 시작 ==========");
		log.info("댓글 조치 로직 실행 대상 commentSn: {}", report.getDclrTrgtContsId());

		String actionType = request.getSanctionType();
		String targetCommentSn = report.getDclrTrgtContsId();
		String commentOwnerUserNo = null;

		if (targetCommentSn != null && !targetCommentSn.isEmpty()) {
			commentOwnerUserNo = adminReportsMapper.getCommentOwnerUserNo(targetCommentSn);
		} else {
			log.warn("댓글 조치 대상 commentSn이 없거나 비어있어 댓글 조치 로직을 건너_ㅂ니다. 신고ID: {}", request.getDclrId());
			log.info("========== processCommentAction 메소드 종료 ==========");
			return;
		}

		switch (actionType) {
		case "삭제 조치":
			Map<String, Object> commentDeleteParams = new HashMap<>();
			commentDeleteParams.put("commentSn", targetCommentSn);
			commentDeleteParams.put("adminId", request.getAdminId()); // [수정] adminId는 request.getAdminId()에서 가져와야 합니다.
			adminReportsMapper.deleteComment(commentDeleteParams);
			log.info("댓글 {} 소프트 삭제 조치 완료. (del_dt, del_user_no 업데이트)", targetCommentSn);
			break;
		case "작성자 제한":
			if (commentOwnerUserNo != null && !commentOwnerUserNo.isEmpty()) {
				adminReportsMapper.updateUserStatus(commentOwnerUserNo, "제한");
				log.info("댓글 작성자 {} 계정 제한 조치 완료.", commentOwnerUserNo);
				handleSanctionForContentOwner(commentOwnerUserNo, actionType, report.getDclrId(), request.getPrcsCn(),
						request.getAdminId(), request.getSanctionDuration());
			} else {
				log.warn("댓글 작성자 userNo를 찾을 수 없어 계정 제한 조치를 할 수 없습니다. 댓글: {}", targetCommentSn);
			}
			break;
		case "제재 없음":
			log.info("댓글 {}에 대해 '제재 없음' 조치 선택.", targetCommentSn);
			break;
		default:
			log.warn("알 수 없는 댓글 조치 유형: {}. 신고ID: {}", actionType, request.getDclrId());
			break;
		}
		log.info("========== processCommentAction 메소드 종료 ==========");
	}

	/**
	 * [구현] 상품 관련 조치를 처리하는 private 메소드 조치 종류: 상품 삭제 조치 / 제재 없음
	 */
	private void processProductAction(ReportProcessRequest request, AdminReportDetail report) {
		log.info("========== processProductAction 메소드 시작 =========="); // [추가] 디버그 로그
		log.info("상품 조치 로직 실행 대상 gdsNo: {}", report.getDclrTrgtContsId());

		String actionType = request.getSanctionType(); // 제재 유형 필드를 조치 유형으로 재활용
		String targetGdsNo = report.getDclrTrgtContsId();
		String productOwnerUserNo = null; // 상품 소유자 (판매자)

		if (targetGdsNo != null && !targetGdsNo.isEmpty()) {
			// 상품의 소유자(판매자) userNo를 조회 (이전 Mapper에 추가했던 getProductOwnerUserNo)
			// 주의: getProductOwnerUserNo가 user_no를 반환하도록 Mapper 쿼리 확인
			productOwnerUserNo = adminReportsMapper.getProductOwnerUserNo(targetGdsNo);
		} else {
			log.warn("상품 조치 대상 gdsNo가 없거나 비어있어 상품 조치 로직을 건너뜁니다. 신고ID: {}", request.getDclrId());
			log.info("========== processProductAction 메소드 종료 ==========");
			return;
		}

		switch (actionType) {
		case "삭제 조치": // 상품 삭제는 노출 여부 변경으로 처리
			adminReportsMapper.updateProductExposureYn(targetGdsNo, false); // false: 노출 안함
			log.info("상품 {} 삭제 조치 완료. (노출 여부 false로 변경)", targetGdsNo);
			break;
		case "제재 없음":
			log.info("상품 {}에 대해 '제재 없음' 조치 선택.", targetGdsNo);
			break;
		default:
			log.warn("알 수 없는 상품 조치 유형: {}. 신고ID: {}", actionType, request.getDclrId());
			break;
		}
		log.info("========== processProductAction 메소드 종료 =========="); // [추가] 디버그 로그
	}

	/**
	 * [신규] 콘텐츠 소유자에게 계정 제한 조치 및 user_sanctions 기록을 남기는 공통 메소드 [수정] sanctionDuration
	 * 파라미터 추가
	 */
	private void handleSanctionForContentOwner(String userNo, String sanctionType, String dclrId, String prcsCn,
			String adminId, String sanctionDuration) { // [수정] sanctionDuration 추가
		log.info("handleSanctionForContentOwner 실행: userNo={}, sanctionType={}, sanctionDuration={}", userNo,
				sanctionType, sanctionDuration); // [수정] 로그에 sanctionDuration 추가

		LocalDateTime startDate = LocalDateTime.now();
		LocalDateTime endDate = null;

		// ★★★ [여기 수정] sanctionDuration 값을 활용하여 endDate 계산 ★★★
		if ("3일".equals(sanctionDuration))
			endDate = startDate.plusDays(3);
		else if ("7일".equals(sanctionDuration))
			endDate = startDate.plusDays(7);
		else if ("14일".equals(sanctionDuration))
			endDate = startDate.plusDays(14);
		else if ("30일".equals(sanctionDuration))
			endDate = startDate.plusDays(30);
		// "영구"일 경우 endDate는 null 유지
		// ★★★ [여기까지 수정] ★★★

		String nextSanctionId = adminReportsMapper.getNextSanctionId();

		UserSanction sanction = new UserSanction();
		sanction.setSanctionId(nextSanctionId);
		sanction.setUserNo(userNo);
		sanction.setDclrId(dclrId);
		sanction.setSanctionType(sanctionDuration + " 이용 정지"); // [수정] sanctionType에 기간 명시 (예: "3일 이용 정지")
		sanction.setSanctionReason("신고 처리 결과에 따른 계정 제한: " + prcsCn);
		sanction.setSanctionStartDt(startDate);
		sanction.setSanctionEndDt(endDate);
		sanction.setPrcsAdminNo(adminId);

		adminReportsMapper.insertSanction(sanction);
		log.info("콘텐츠 소유자 {} 계정 제한 기록 (sanctionId: {}) user_sanctions에 저장 완료.", userNo, nextSanctionId);
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