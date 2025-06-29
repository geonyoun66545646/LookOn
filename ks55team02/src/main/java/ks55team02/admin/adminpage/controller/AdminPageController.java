package ks55team02.admin.adminpage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("")
public class AdminPageController {

    // 메인 페이지
	@GetMapping("/adminpage")
	public String AdminHomeController() {
		
		return "admin/main";
	}
}
