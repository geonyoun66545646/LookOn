package ks55team02.admin.adminpage.inquiryadmin.reports.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReportDetail;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReportSearch;
import ks55team02.admin.adminpage.inquiryadmin.reports.service.AdminReportsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
// [유지] 파트너님의 기존 경로를 그대로 유지합니다.
@RequestMapping("/adminpage/inquiryadmin")
@RequiredArgsConstructor
public class AdminReportsController {

	private final AdminReportsService adminReportsService;

	// [유지] 목록 조회 메소드는 전혀 건드리지 않습니다. 잘 동작하고 있습니다.
	@GetMapping("/reportsList")
	public String getAdminReportsList(@ModelAttribute AdminReportSearch searchCriteria, Model model) {
		Map<String, Object> resultMap = adminReportsService.getAdminReportList(searchCriteria);
		model.addAttribute("reportList", resultMap.get("reportList"));
		model.addAttribute("pagination", resultMap.get("pagination"));
		model.addAttribute("title", "신고 목록 조회");
		return "admin/adminpage/inquiryadmin/reportsList";
	}

	/*
	 * [삭제] 이 메소드들은 이제 필요 없으므로 과감하게 삭제합니다.
	 * 
	 * @GetMapping("/reportsDetail/{dclrId}") ... getAdminReportsDetail(...)
	 * 
	 * @GetMapping("/reportsDetail") ... getAdminReportsDetailTest()
	 */

	/**
	 * GET 요청을 통해 신고 상세 페이지를 로드하는 메소드 (이 메소드만 남기고 수정합니다)
	 * 
	 * @param dclrId URL 경로에서 받아온 신고 아이디
	 * @param model  View에 데이터를 전달하기 위한 Model 객체
	 * @return 보여줄 View의 논리적인 이름
	 */
	// [수정 1] URL 경로를 JavaScript와 일치하도록 명확하게 지정합니다.
	@GetMapping("/reports/{dclrId}")
	public String adminReportDetail(@PathVariable(name = "dclrId") String dclrId, Model model) {

		log.info("상세 조회를 위해 컨트롤러가 받은 dclrId: {}", dclrId);

		AdminReportDetail reportDetail = adminReportsService.getAdminReportDetail(dclrId);
		model.addAttribute("reportDetail", reportDetail);
		model.addAttribute("title", "신고 상세 조회");

		// [수정 2] 반환하는 html 파일 경로를 정확하게 지정합니다.
		return "admin/adminpage/inquiryadmin/reportsDetail";
	}
}