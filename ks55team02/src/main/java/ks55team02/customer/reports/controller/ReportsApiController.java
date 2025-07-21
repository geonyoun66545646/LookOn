package ks55team02.customer.reports.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.customer.reports.domain.Reports;
import ks55team02.customer.reports.domain.ReportsReasons;
import ks55team02.customer.reports.service.ReportsService;
import ks55team02.util.CustomerPagination;

@RestController
@RequestMapping("/api/customer/reports")
public class ReportsApiController {

	private static final Logger log = LoggerFactory.getLogger(ReportsApiController.class);

	private final ReportsService reportsService;

	public ReportsApiController(ReportsService reportsService) {
		this.reportsService = reportsService;
	}

	// 사용자 로그인했는지 확인
	@GetMapping("/check-auth")
	public ResponseEntity<Void> checkAuthentication(HttpSession session) {
		// 세션에서 "loginUser" 속성을 가져옵니다.
		Object loginUser = session.getAttribute("loginUser");

		// "loginUser" 속성이 없으면(비로그인 상태이면)
		if (loginUser == null) {
			// HTTP 상태 코드 401 (Unauthorized)을 응답합니다.
			// 클라이언트(JavaScript)는 이 코드를 보고 로그인되지 않았음을 알 수 있습니다.
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		// "loginUser" 속성이 있으면(로그인 상태이면)
		// HTTP 상태 코드 200 (OK)을 응답합니다.
		return ResponseEntity.ok().build();
	}

	@GetMapping("/target-types")
	public ResponseEntity<List<String>> getReportTargetTypeList() {
		try {
			List<String> targetTypeList = reportsService.getReportTargetTypeList();
			return ResponseEntity.ok(targetTypeList);
		} catch (Exception e) {
			log.error("신고 대상 유형 목록 조회 중 에러 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@GetMapping("/reasons/{targetType}")
	public ResponseEntity<List<ReportsReasons>> getReportReasons(@PathVariable String targetType) {
		try {
			List<ReportsReasons> reasonList = reportsService.getActiveReportReasonList(targetType);
			return ResponseEntity.ok(reasonList);
		} catch (Exception e) {
			log.error("신고 사유 목록 조회 중 에러 발생: targetType={}", targetType, e);
			// 에러 발생 시 빈 목록과 함께 500 에러 응답
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@PostMapping
	public ResponseEntity<Map<String, Object>> addReport(Reports report,
			@RequestParam(value = "evidenceFile", required = false) List<MultipartFile> evidenceFiles,
			HttpSession session) {

		Map<String, Object> response = new HashMap<>();

		Object sessionUser = session.getAttribute("loginUser");
		if (sessionUser == null) {
			// ... (기존 에러 처리 로직)
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		try {
			String userNo = ((LoginUser) sessionUser).getUserNo();
			report.setDclrUserNo(userNo);

			// 서비스에 report 객체와 파일 목록을 함께 전달합니다.
			reportsService.addReport(report, evidenceFiles);

			response.put("success", true);
			response.put("message", "신고가 성공적으로 접수되었습니다.");
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("신고 접수 처리 중 에러 발생", e);
			response.put("success", false);
			response.put("message", "서버 내부 오류로 신고 접수에 실패했습니다: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	/**
	 * [수정] 현재 로그인한 사용자의 신고 내역 목록을 '검색' 및 '페이지네이션'하여 조회하는 API
	 * 
	 * @param searchKeyword 검색어 (선택 사항)
	 * @param page          현재 페이지 번호 (선택 사항, 기본값 1)
	 * @param session       현재 세션 정보
	 * @return 페이징 처리된 신고 내역 데이터 (CustomerPagination 객체)
	 */
	@GetMapping("/my-list")
	public ResponseEntity<CustomerPagination<Map<String, Object>>> getMyReportList(
			@RequestParam(value = "searchKeyword", required = false, defaultValue = "") String searchKeyword,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page, HttpSession session) {

		// 1. 세션에서 로그인 사용자 정보 확인
		Object sessionUser = session.getAttribute("loginUser");
		if (sessionUser == null) {
			log.warn("인증되지 않은 사용자가 '내 신고 내역' API를 호출했습니다.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		try {
			// 2. 로그인한 사용자의 고유 번호(userNo) 추출
			String userNo = ((LoginUser) sessionUser).getUserNo();
			log.info("내 신고 내역 조회 요청: userNo={}, page={}, searchKeyword='{}'", userNo, page, searchKeyword);

			// 3. 서비스 레이어에 작업을 위임하여 페이징된 데이터 가져오기
			CustomerPagination<Map<String, Object>> pagination = reportsService.getMyReportList(userNo, searchKeyword,
					page);

			// 4. 조회된 데이터를 200 OK 상태와 함께 클라이언트에게 반환
			return ResponseEntity.ok(pagination);

		} catch (Exception e) {
			log.error("내 신고 내역 조회 중 서버 에러 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * 특정 신고 1건의 상세 정보를 조회하는 API
	 * 
	 * @param dclrId  조회할 신고의 고유 ID
	 * @param session 현재 세션 정보 (본인 확인용)
	 * @return 신고 상세 정보 (JSON)
	 */
	@GetMapping("/{dclrId}")
	public ResponseEntity<Map<String, Object>> getReportDetail(@PathVariable String dclrId, HttpSession session) {

		// 1. 세션에서 로그인 사용자 정보 확인 (인증)
		Object sessionUser = session.getAttribute("loginUser");
		if (sessionUser == null) {
			log.warn("인증되지 않은 사용자가 상세 신고 내역 API에 접근했습니다. (dclrId: {})", dclrId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		try {
			// 2. 서비스 레이어에 작업을 위임하여 신고 상세 데이터 가져오기
			Map<String, Object> reportDetail = reportsService.getReportDetail(dclrId);

			// 3. 조회된 신고가 없는 경우, 404 Not Found 응답
			if (reportDetail == null || reportDetail.isEmpty()) {
				log.warn("존재하지 않는 신고 내역에 접근 시도 (dclrId: {})", dclrId);
				return ResponseEntity.notFound().build();
			}

			// 4. 본인의 신고 내역이 맞는지 확인 (인가/권한 확인)
			String currentUserNo = ((LoginUser) sessionUser).getUserNo();
			String reportOwnerUserNo = (String) reportDetail.get("dclrUserNo");

			if (!currentUserNo.equals(reportOwnerUserNo)) {
				log.warn("권한 없는 사용자가 타인의 신고 내역에 접근 시도. (요청자: {}, 신고 소유자: {}, dclrId: {})", currentUserNo,
						reportOwnerUserNo, dclrId);
				// 다른 사람의 정보이므로, "찾을 수 없음"으로 응답하여 정보 노출을 막습니다.
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}

			// 5. 모든 확인 절차 통과 시, 조회된 데이터를 200 OK 상태와 함께 반환
			return ResponseEntity.ok(reportDetail);

		} catch (Exception e) {
			log.error("신고 상세 내역 조회 중 서버 에러 발생 (dclrId: {})", dclrId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

}
