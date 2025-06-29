package ks55team02.admin.adminpage.superadmin.admincreate.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/superadmin")
public class AdminCreateController {

	// 총관리자
	@GetMapping("/adminCreate")
	public String superadminCreateController() {
		
		return "admin/adminpage/superadmin/adminCreate";
	}
}
