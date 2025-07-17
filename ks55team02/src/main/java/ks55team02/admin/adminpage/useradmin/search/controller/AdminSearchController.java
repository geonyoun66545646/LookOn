package ks55team02.admin.adminpage.useradmin.search.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/useradmin")
public class AdminSearchController {

	@GetMapping("/search")
	public String useradminSearchController(Model model) { // Model 파라미터 추가
	    model.addAttribute("title", "검색 기록 조회"); // Model에 title 값 추가
	    return "admin/adminpage/useradmin/adminSearch";
	}
}
