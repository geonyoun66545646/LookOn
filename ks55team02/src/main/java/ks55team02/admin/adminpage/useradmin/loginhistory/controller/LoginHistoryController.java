package ks55team02.admin.adminpage.useradmin.loginhistory.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import ks55team02.admin.adminpage.useradmin.loginhistory.domain.LoginHistory;
import ks55team02.admin.adminpage.useradmin.loginhistory.service.LoginHistoryService;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 페이지 내 사용자 로그인 기록 관련 요청을 처리하는 컨트롤러 클래스입니다.
 * 로그인 기록 조회 및 검색/페이징 기능을 제공합니다.
 */
@Controller
@RequestMapping("/adminpage/useradmin")
@RequiredArgsConstructor
public class LoginHistoryController {

	private final LoginHistoryService loginHistoryService;
	
	/**
	 * 로그인 기록 조회 페이지의 최초 로딩을 처리하는 GET 요청 핸들러 메소드입니다.
	 * 서비스 계층을 통해 로그인 기록 데이터를 로드하고, 모델에 추가하여 뷰로 전달합니다.
	 *
	 * @param criteria 검색 및 페이징 조건을 담는 {@link LoginHistory} DTO. `@ModelAttribute`를 통해 HTTP 요청 파라미터가 자동으로 바인딩됩니다.
	 * @param model    뷰로 데이터를 전달할 Spring UI Model 객체
	 * @return         로그인 기록 조회 페이지의 뷰 경로 (templates/admin/adminpage/useradmin/loginHistory.html)
	 */
	@GetMapping("/loginHistory")
	public String useradminLoginHistoryController(@ModelAttribute LoginHistory criteria, Model model) {
		loginHistoryService.loadLoginHistory(criteria, model);
		
		model.addAttribute("title", "로그인기록조회");
		
		return "admin/adminpage/useradmin/loginHistory";
	}
	
	/**
	 * 로그인 기록에 대한 AJAX 검색/페이징 요청을 처리하는 GET 요청 핸들러 메소드입니다.
	 * 클라이언트에서 비동기적으로 데이터 갱신을 요청할 때 사용됩니다.
	 * 서비스 계층을 통해 데이터를 로드한 후, Thymeleaf Fragment를 사용하여 페이지의 특정 부분만 업데이트합니다.
	 *
	 * @param criteria 검색 및 페이징 조건을 담는 {@link LoginHistory} DTO
	 * @param model    뷰로 데이터를 전달할 Spring UI Model 객체
	 * @return         갱신될 뷰의 경로 (admin/adminpage/useradmin/loginHistory :: #userListContainer)
	 */
    @GetMapping("/loginHistory/search")
    public String searchLoginHistory(@ModelAttribute LoginHistory criteria, Model model) {
        loginHistoryService.loadLoginHistory(criteria, model);
        return "admin/adminpage/useradmin/loginHistory :: #userListContainer"; 
    }
}
