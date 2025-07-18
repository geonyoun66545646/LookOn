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
}