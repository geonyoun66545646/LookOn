package ks55team02.customer.mypage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.customer.login.domain.UserInfoResponse;
import ks55team02.customer.login.service.UserInfoService;
import ks55team02.customer.mypage.service.UserWithdrawalService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CustomerController {
	
    private final UserInfoService userInfoService;
    private final UserWithdrawalService userWithdrawalService;
	
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
	
	/**
     * 회원 탈퇴 페이지를 보여주는 요청을 처리합니다.
     * @return 회원 탈퇴 페이지 뷰 경로
     */
    @GetMapping("/mypage/withdraw")
    public String withdrawPage() {
        return "customer/mypage/withdraw"; // 위에서 만든 HTML 파일 경로
    }

    /**
     * 실제 회원 탈퇴 요청을 처리합니다.
     * @param password           사용자가 입력한 비밀번호
     * @param session            현재 세션
     * @param redirectAttributes 리다이렉트 시 메시지 전달용
     * @return 성공/실패에 따른 리다이렉트 경로
     */
    @PostMapping("/mypage/withdraw")
    public String processWithdraw(
            @RequestParam("password") String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/";
        }

        try {
            // 2. [변경 없음] userWithdrawalService의 메소드를 호출합니다.
            userWithdrawalService.withdrawUser(loginUser, password);

            session.invalidate();
            redirectAttributes.addFlashAttribute("message", "회원 탈퇴가 정상적으로 처리되었습니다.");
            return "redirect:/";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/mypage/withdraw";
        }
    }
}
