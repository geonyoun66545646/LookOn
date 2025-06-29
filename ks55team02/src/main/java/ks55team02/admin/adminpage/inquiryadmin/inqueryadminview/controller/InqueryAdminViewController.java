package ks55team02.admin.adminpage.inquiryadmin.inqueryadminview.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/inquiryadmin")
public class InqueryAdminViewController {

	// 문의 및 신고 관리자
	@GetMapping("/inqueryAdminView")
	public String inquiryAdminRepotController() {
		
		return "admin/adminpage/inquiryadmin/inqueryAdminView";
	}
}
