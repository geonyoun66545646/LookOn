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
	 * 신고 처리 요청을 받아 처리하는 API
	 * 
	 * @param dclrId  처리할 신고의 ID
	 * @param request 처리 내용과 새로운 상태가 담긴 DTO
	 * @return 처리 결과 (성공/실패 메시지)
	 */
	@PostMapping("/{dclrId}/process")
	public ResponseEntity<Map<String, Object>> processReport(@PathVariable(name = "dclrId") String dclrId,
			@RequestBody ReportProcessRequest request, HttpSession session) {

		// [최종 수정] AdminLoginController에서 확인한 정보와 완벽하게 일치시킵니다.
		// 1. 세션에서 "loginUser"라는 이름으로 정보를 가져옵니다.
		LoginUser loginAdmin = (LoginUser) session.getAttribute("loginUser");

		// 2. 만약 로그인 정보가 없으면, 권한 없음(401) 에러를 응답합니다.
		if (loginAdmin == null) {
			Map<String, Object> response = new HashMap<>();
			response.put("message", "관리자 로그인이 필요한 기능입니다.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		// 3. LoginUser 객체의 getUserNo() 메소드를 호출하여 관리자 ID를 가져옵니다.
		String adminId = loginAdmin.getUserNo();
		request.setAdminId(adminId);
		request.setDclrId(dclrId);

		// =====================================================================
		// [핵심 진단 코드] Service를 호출하기 직전에, DTO의 모든 내용을 로그로 출력합니다.
		// =====================================================================
		log.info("▶▶▶ Service로 전달될 최종 데이터 확인: {}", request.toString());

		adminReportsService.processReport(request);

		Map<String, Object> response = new HashMap<>();
		response.put("message", "신고가 성공적으로 처리되었습니다.");
		return ResponseEntity.ok(response);
	}

	/**
	 * 신고 목록 데이터를 JSON 형태로 조회
	 * 
	 * @param searchCriteria 검색 및 페이징 조건
	 * @return Map 형태의 JSON 데이터 (reportList, pagination 포함)
	 */
	@GetMapping
	public Map<String, Object> getReportList(@ModelAttribute AdminReportSearch searchCriteria) {
		// 기존 서비스 로직을 그대로 재사용하여 데이터만 반환합니다.
		return adminReportsService.getAdminReportList(searchCriteria);
	}

	// ================== ▼▼▼ 바로 이 메서드를 추가해야 합니다 ▼▼▼ ==================
	/**
	 * 특정 신고의 상세 정보를 JSON 형태로 조회하는 API (AJAX 호출용)
	 * 
	 * @param dclrId 조회할 신고 ID
	 * @return AdminReportDetail 객체를 포함한 ResponseEntity
	 */
	@GetMapping("/{dclrId}")
	public ResponseEntity<Object> getReportDetailForAjax(@PathVariable(name = "dclrId") String dclrId) {

		AdminReportDetail reportDetail = adminReportsService.getAdminReportDetail(dclrId);

		if (reportDetail != null) {
			// 데이터가 있으면 JSON 데이터와 함께 200 OK 응답
			return ResponseEntity.ok(reportDetail);
		} else {
			// 데이터가 없으면 404 Not Found 응답
			Map<String, Object> response = new HashMap<>();
			response.put("message", "해당 신고 정보를 찾을 수 없습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
	}
	// ================== ▲▲▲ 여기까지 추가 ▲▲▲ ==================
}