package ks55team02.admin.adminpage.inquiryadmin.reports.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReport;
import ks55team02.admin.adminpage.inquiryadmin.reports.service.AdminReportService;
import ks55team02.admin.common.domain.Pagination;
import ks55team02.admin.common.domain.SearchCriteria;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/adminpage/inquiryadmin") // 1. 공통 경로를 여기까지로 수정합니다.
@RequiredArgsConstructor
public class AdminReportsController {

	// ... (내용은 그대로) ...
	private final AdminReportService adminReportService;

	// 2. 목록 조회 메소드에 나머지 주소인 "/reportsList"를 직접 지정합니다.
	@GetMapping("/reportsList")
	public String getAdminReportsList(@ModelAttribute SearchCriteria searchCriteria, Model model) {
		Map<String, Object> resultMap = adminReportService.getAdminReportList(searchCriteria);

		model.addAttribute("reportList", resultMap.get("reportList"));
		model.addAttribute("pagination", resultMap.get("pagination"));
		model.addAttribute("title", "신고 목록 조회");

		return "admin/adminpage/inquiryadmin/reportsList";
	}

	// 3. 상세 페이지 주소도 /reportsDetail/{dclrId}로 명확하게 지정
	@GetMapping("/reportsDetail/{dclrId}")
	public String getAdminReportsDetail(@PathVariable(name = "dclrId") String dclrId, Model model) {

		model.addAttribute("title", "신고 상세 조회");

		return "admin/adminpage/inquiryadmin/reportsDetail";
	}
	/*
	 * [추가] 순수 HTML 화면 확인을 위한 임시 테스트 메소드
	 * 나중에 기능 개발이 완료되면 지워도 됩니다.
	 */
	@GetMapping("/reportsDetail")
	public String getAdminReportsDetailTest() {
	    return "admin/adminpage/inquiryadmin/reportsDetail";
	}

}