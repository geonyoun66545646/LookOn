package ks55team02.admin.adminpage.inquiryadmin.reportslist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/inquiryadmin")
public class ReportsListController {

	@GetMapping("/reportsList")
	public String getAdminPageReportsList() {
		
		return "admin/adminpage/inquiryadmin/reportsList";
	}
}
