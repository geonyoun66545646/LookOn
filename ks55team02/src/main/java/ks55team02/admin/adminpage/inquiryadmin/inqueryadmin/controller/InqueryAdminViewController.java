package ks55team02.admin.adminpage.inquiryadmin.inqueryadmin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/inquiryadmin")
public class InqueryAdminViewController {

	// 문의 관리
	@GetMapping("/inquiryAdminView")
	public String inquiryAdminRepotController() {
		
		return "admin/adminpage/inquiryadmin/inqueryAdminView";
	}
}
