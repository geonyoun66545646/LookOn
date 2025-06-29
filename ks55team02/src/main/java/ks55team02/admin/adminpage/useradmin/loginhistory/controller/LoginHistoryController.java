package ks55team02.admin.adminpage.useradmin.loginhistory.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/useradmin")
public class LoginHistoryController {

	// 로그인 기록
	@GetMapping("/loginHistory")
	public String useradminLoginHistoryController() {
		
		return "admin/adminpage/useradmin/loginHistory";
	}
}
