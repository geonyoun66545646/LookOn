package ks55team02.admin.dashboard.controller;



import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ks55team02.admin.dashboard.domain.Dashboard;
import ks55team02.admin.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/adminpage")
@RequiredArgsConstructor
public class DashBoardController {
	
	private final DashboardService dashboardService;

	@GetMapping("/dashboard")
	public String mainDashboard(Model model) {
		
		// 1. Service를 호출하여 대시보드에 필요한 모든 데이터가 담긴 Dashboard 객체를 받습니다.
        Dashboard dashboardData = dashboardService.getDashboardData();

        // 2. Model 객체에 조회한 데이터를 담아 View(HTML)로 전달합니다.
        //    HTML에서는 "dashboard"라는 이름으로 이 객체를 사용할 수 있습니다.
        model.addAttribute("dashboard", dashboardData);
		
		return "admin/dashboard/dashBoard";
	}
}
