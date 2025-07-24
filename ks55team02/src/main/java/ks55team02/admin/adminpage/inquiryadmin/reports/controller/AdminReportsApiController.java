package ks55team02.admin.adminpage.inquiryadmin.reports.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReportDetail;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReportSearch;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.ReportProcessRequest;
import ks55team02.admin.adminpage.inquiryadmin.reports.service.AdminReportsService;
import ks55team02.customer.login.domain.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportsApiController {

	private final AdminReportsService adminReportsService;

	/**
	 * 신고 처리 요청을 받아 처리하는 API (수정 없음)
	 */
	@PostMapping("/{dclrId}/process")
	public ResponseEntity<Map<String, Object>> processReport(@PathVariable(name = "dclrId") String dclrId,
			@RequestBody ReportProcessRequest request, HttpSession session) {

		LoginUser loginAdmin = (LoginUser) session.getAttribute("loginUser");

		if (loginAdmin == null) {
			Map<String, Object> response = new HashMap<>();
			response.put("message", "관리자 로그인이 필요한 기능입니다.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String adminId = loginAdmin.getUserNo();
		request.setAdminId(adminId);
		request.setDclrId(dclrId);

		log.info("▶▶▶ Service로 전달될 최종 데이터 확인: {}", request.toString());

		adminReportsService.processReport(request);

		Map<String, Object> response = new HashMap<>();
		response.put("message", "신고가 성공적으로 처리되었습니다.");
		return ResponseEntity.ok(response);
	}

	/**
	 * 신고 목록 데이터를 JSON 형태로 조회 (수정)
	 */
	@GetMapping
	// ▼▼▼ [수정] @ModelAttribute를 제거하고, 디버깅을 위한 로그를 추가했습니다. ▼▼▼
	public Map<String, Object> getReportList(AdminReportSearch searchCriteria) {

		// [추가] 프론트엔드에서 보낸 검색 조건이 DTO에 잘 담겼는지 확인하는 로그
		log.info("API 컨트롤러가 받은 검색 조건: {}", searchCriteria);

		return adminReportsService.getAdminReportList(searchCriteria);
	}

	/**
	 * 특정 신고의 상세 정보를 JSON 형태로 조회하는 API (수정 없음)
	 */
	@GetMapping("/{dclrId}")
	public ResponseEntity<Object> getReportDetailForAjax(@PathVariable(name = "dclrId") String dclrId) {

		AdminReportDetail reportDetail = adminReportsService.getAdminReportDetail(dclrId);

		if (reportDetail != null) {
			return ResponseEntity.ok(reportDetail);
		} else {
			Map<String, Object> response = new HashMap<>();
			response.put("message", "해당 신고 정보를 찾을 수 없습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
	}
}