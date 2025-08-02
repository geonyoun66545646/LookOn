package ks55team02.admin.adminpage.inquiryadmin.reports.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
	 /**
     * [신규 추가] 처리 완료된 신고 내용을 수정하는 API
     * @param dclrId URL 경로에서 받아온 신고 아이디
     * @param request 수정할 내용을 담은 요청 객체
     * @param session 현재 로그인한 관리자 정보를 얻기 위한 세션
     * @return 처리 결과 메시지를 담은 ResponseEntity
     */
    @PutMapping("/{dclrId}/update") // JavaScript에서 호출한 대로 PUT 메소드로 받습니다.
    public ResponseEntity<Map<String, Object>> updateReport(
            @PathVariable(name = "dclrId") String dclrId,
            @RequestBody ReportProcessRequest request,
            HttpSession session) {

        // 1. 관리자 로그인 상태를 확인하는 것은 '처리'와 동일하게 중요합니다.
        LoginUser loginAdmin = (LoginUser) session.getAttribute("loginUser");
        if (loginAdmin == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "관리자 로그인이 필요한 기능입니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 2. request 객체에 필요한 값(신고 ID, 관리자 ID)을 설정합니다.
        String adminId = loginAdmin.getUserNo();
        request.setAdminId(adminId);
        request.setDclrId(dclrId);

        log.info("▶▶▶ '수정' Service로 전달될 최종 데이터 확인: {}", request.toString());

        try {
            // 3. '수정'을 전담할 새로운 서비스 메소드를 호출합니다.
            adminReportsService.updateProcessedReport(request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "신고 내용이 성공적으로 수정되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            // Service에서 발생할 수 있는 예외(예: 수정 불가능한 상태)를 처리합니다.
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}