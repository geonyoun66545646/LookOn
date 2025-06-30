package ks55team02.admin.adminpage.inquiryadmin.reportprocess.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/inquiryadmin")
public class ReportProcessController {

	@GetMapping("/reportProcess")
	public String boardAdmincommentController() {
		
		return "admin/adminpage/inquiryadmin/reportProcess";
	}
}
