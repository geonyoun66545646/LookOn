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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	public ResponseEntity<Map<String, Object>> addReport(@RequestBody Reports report) {
		Map<String, Object> response = new HashMap<>();
		try {
			// TODO: 실제 로그인한 사용자의 ID를 가져와서 설정해야 합니다.
			// Spring Security 등을 사용한다면 Principal 객체에서 사용자 정보를 얻을 수 있습니다.
			// 예: report.setDclrUserNo(loggedInUserId);
			// 지금은 프론트에서 넘어온 값을 그대로 사용합니다.

			log.info("신고 접수 요청 데이터: {}", report);
			reportsService.addReport(report);

			response.put("success", true);
			response.put("message", "신고가 성공적으로 접수되었습니다.");
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("신고 접수 처리 중 에러 발생", e);

			response.put("success", false);
			response.put("message", "서버 내부 오류로 신고 접수에 실패했습니다. 관리자에게 문의해주세요.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
}