package ks55team02.admin.adminpage.superadmin.authrt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/superadmin")
public class AuthrtController {

	@GetMapping("/authrt")
	public String superadminAuthrtController() {
		
		return "admin/adminpage/superadmin/authrt";
	}
}
