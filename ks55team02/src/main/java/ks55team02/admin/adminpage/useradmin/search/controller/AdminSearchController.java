package ks55team02.admin.adminpage.useradmin.search.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/useradmin")
public class AdminSearchController {

	// 검색기록
	@GetMapping("/search")
	public String useradminSearchController() {
		
		return "admin/adminpage/useradmin/adminSearch";
	}
}
