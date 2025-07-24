package ks55team02.admin.dashboard.controller;



import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ks55team02.admin.dashboard.domain.Dashboard;
import ks55team02.admin.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 대시보드 관련 요청을 처리하는 컨트롤러 클래스입니다.
 * 관리자 대시보드 페이지를 표시하고 필요한 데이터를 모델에 추가합니다.
 */
@Controller
@RequestMapping("/adminpage")
@RequiredArgsConstructor
public class DashBoardController {
	
	private final DashboardService dashboardService;

	/**
	 * 관리자 대시보드 메인 페이지를 보여주는 GET 요청 핸들러 메소드입니다.
	 * 대시보드에 필요한 모든 데이터를 서비스로부터 받아와 모델에 추가하여 뷰로 전달합니다.
	 *
	 * @param model 뷰로 데이터를 전달하기 위한 Spring UI Model 객체
	 * @return      관리자 대시보드 페이지의 뷰 경로 (templates/admin/dashboard/dashBoard.html)
	 */
	@GetMapping("/dashboard")
	public String mainDashboard(Model model) {
		
		// 1. DashboardService를 호출하여 대시보드에 필요한 모든 통계 및 현황 데이터가 담긴 Dashboard 객체를 가져옵니다.
        Dashboard dashboardData = dashboardService.getDashboardData();

        // 2. Model 객체에 조회한 dashboardData를 "dashboard"라는 이름으로 추가합니다.
        //    이렇게 추가된 데이터는 Thymeleaf 템플릿(admin/dashboard/dashBoard.html)에서 'dashboard' 변수로 접근하여 사용할 수 있습니다.
        model.addAttribute("dashboard", dashboardData);
		
		return "admin/dashboard/dashBoard";
	}
}
