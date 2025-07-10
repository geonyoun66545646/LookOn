package ks55team02.admin.adminpage.inquiryadmin.reportsdetail.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/inquiryadmin")
public class ReportsDetailController {

	@GetMapping("/reportsDetail")
	public String getAdminPageReportsDetail() {
		
		return "admin/adminpage/inquiryadmin/reportsDetail";
	}
}
