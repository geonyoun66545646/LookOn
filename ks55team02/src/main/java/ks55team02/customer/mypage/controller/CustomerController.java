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

/**
 * 고객 마이페이지 및 관련 기능을 처리하는 컨트롤러 클래스입니다.
 * 마이페이지 조회, 회원 정보 수정 페이지 표시, 회원 탈퇴 기능을 제공합니다.
 */
@Controller
@RequiredArgsConstructor
public class CustomerController {
	
    private final UserInfoService userInfoService;
    private final UserWithdrawalService userWithdrawalService;
	
    /**
	 * 고객 마이페이지를 보여주는 GET 요청 핸들러 메소드입니다.
	 * 세션에서 로그인 사용자 정보를 확인하고, 해당 사용자의 상세 정보를 조회하여 모델에 추가합니다.
	 *
	 * @param session HTTP 세션 객체. 로그인 사용자 정보를 가져오는 데 사용됩니다.
	 * @param model   뷰로 데이터를 전달할 Spring UI Model 객체
	 * @return        고객 마이페이지 뷰 경로 (customer/mypage/customerMyPage.html)
	 */
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
	
	/**
	 * 회원 정보 수정 페이지를 보여주는 GET 요청 핸들러 메소드입니다.
	 * 세션에서 로그인 사용자 정보를 확인하고, 유효하지 않으면 로그인 페이지로 리다이렉트합니다.
	 * 실제 사용자 데이터는 이 페이지의 JavaScript가 API를 호출하여 채우는 것으로 가정합니다.
	 *
	 * @param session HTTP 세션 객체. 로그인 사용자 정보를 확인하는 데 사용됩니다.
	 * @param model   뷰로 데이터를 전달할 Spring UI Model 객체 (현재는 사용되지 않음)
	 * @return        회원 정보 수정 페이지 뷰 경로 (customer/info/customerEditInfo.html)
	 */
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
     * 회원 탈퇴 페이지를 보여주는 GET 요청 핸들러 메소드입니다.
     * 사용자가 회원 탈퇴를 진행하기 전에 확인하는 페이지를 제공합니다.
     *
     * @return 회원 탈퇴 페이지 뷰 경로 (customer/mypage/withdraw.html)
     */
    @GetMapping("/mypage/withdraw")
    public String withdrawPage() {
        return "customer/mypage/withdraw"; // 위에서 만든 HTML 파일 경로
    }

    /**
     * 실제 회원 탈퇴 요청을 처리하는 POST 요청 핸들러 메소드입니다.
     * 사용자가 입력한 비밀번호를 검증하고, 회원 탈퇴 비즈니스 로직을 수행합니다.
     * 성공 시 세션을 무효화하고 메인 페이지로 리다이렉트하며, 실패 시 에러 메시지를 전달합니다.
     *
     * @param password           사용자가 탈퇴 확인을 위해 입력한 비밀번호
     * @param session            현재 사용자의 HTTP 세션 객체
     * @param redirectAttributes 리다이렉트 시 일회성 메시지(Flash Attribute)를 전달하기 위한 객체
     * @return                   탈퇴 성공 시 메인 페이지로, 실패 시 회원 탈퇴 페이지로 리다이렉트하는 경로
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
