package ks55team02.admin.adminpage.useradmin.loginhistory.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import ks55team02.admin.adminpage.useradmin.loginhistory.domain.LoginHistory;
import ks55team02.admin.adminpage.useradmin.loginhistory.service.LoginHistoryService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/adminpage/useradmin")
@RequiredArgsConstructor
public class LoginHistoryController {

	private final LoginHistoryService loginHistoryService;
	
	// 로그인 기록
	@GetMapping("/loginHistory")
	public String useradminLoginHistoryController(@ModelAttribute LoginHistory criteria, Model model) {
		loginHistoryService.loadLoginHistory(criteria, model);
		
		model.addAttribute("title", "로그인기록조회");
		
		return "admin/adminpage/useradmin/loginHistory";
	}
	
	// AJAX 검색/페이징 요청 처리
    @GetMapping("/loginHistory/search")
    public String searchLoginHistory(@ModelAttribute LoginHistory criteria, Model model) {
        loginHistoryService.loadLoginHistory(criteria, model);
        // 목록 영역 전체를 리로드하기 위해 userListFragment를 반환
        return "admin/adminpage/log/loginHistory :: userListFragment"; 
    }
}
