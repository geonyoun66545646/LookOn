package ks55team02.admin.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage")
public class DashBoard {

	@GetMapping("/dashboard")
	public String DashBoardController() {
		
		return "admin/dashboard/dashBoard";
	}
}
