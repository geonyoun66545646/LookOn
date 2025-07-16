package ks55team02.customer.mypage.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.customer.login.domain.UserInfoResponse;
import ks55team02.customer.login.service.UserInfoService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CustomerController {
	
    private final UserInfoService userInfoService;
	
	@GetMapping("/customer/mypage")
	public String CustomerMyPageController(HttpSession session, Model model) {
		// 세션 처리
    	Object sessionObj = session.getAttribute("loginUser");
    	if(sessionObj == null) {
    		return "redirect:/customer/mypage";
    	}
    	// 세션 처리 끝
    	LoginUser loginUser = (LoginUser) sessionObj;
    	
    	
    	String userNo = loginUser.getUserNo();
    	UserInfoResponse userInfo = userInfoService.getUserInfo(userNo);
    	
    	model.addAttribute("userInfo", userInfo);
		
		return "customer/mypage/customerMyPage";
	}
	
	@GetMapping("/editInfo")
    public String editInfoPage(HttpSession session, Model model) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/customer/login";
        }
        // 이 페이지는 JavaScript가 API를 호출하여 데이터를 채우므로,
        // 여기서는 페이지를 보여주는 역할만 합니다.
        return "customer/info/customerEditInfo"; // customerEditInfo.html 파일을 보여줌
    }
	
}
