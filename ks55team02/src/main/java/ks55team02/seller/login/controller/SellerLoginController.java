package ks55team02.seller.login.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.seller.login.domain.SellerLoginRequest;
import ks55team02.seller.login.service.SellerLoginService;
import lombok.RequiredArgsConstructor;

/**
 * 판매자 로그인 관련 요청을 처리하는 컨트롤러 클래스입니다.
 * 판매자 로그인 페이지 표시 및 로그인 처리를 담당합니다.
 */
@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerLoginController {

	private final SellerLoginService sellerLoginService;

	/**
     * 판매자 로그인 페이지 화면을 제공하는 GET 요청 핸들러 메소드입니다.
     *
     * @return 판매자 로그인 페이지의 뷰 경로 (templates/seller/login/sellerLogin.html)
     */
    @GetMapping("/login")
    public String sellerLoginPage() {
        // templates/seller/login/sellerLogin.html 파일을 찾아서 보여줍니다.
        return "seller/login/sellerLogin";
    }

    /**
     * 판매자 로그인 폼에서 전송된 데이터를 처리하는 POST 요청 핸들러 메소드입니다.
     * 로그인 요청을 받아 인증 처리 후 성공/실패에 따라 리다이렉트합니다.
     *
     * @param loginRequest     로그인 폼에서 전송된 아이디, 비밀번호 등을 담은 {@link SellerLoginRequest} DTO
     * @param session          현재 사용자의 HTTP 세션 객체 (로그인 정보 저장에 사용)
     * @param redirectAttributes 리다이렉트 시 일회성 메시지(Flash Attribute)를 전달하기 위한 객체
     * @return                 로그인 성공 시 판매자 메인 대시보드로, 실패 시 로그인 페이지로 리다이렉트하는 뷰 경로
     */
    @PostMapping("/login")
    public String sellerLoginProcess(
            SellerLoginRequest loginRequest,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
    	// SellerLoginService를 호출하여 로그인 로직을 수행하고 로그인 사용자 정보를 가져옵니다.
        LoginUser loginSeller = sellerLoginService.login(loginRequest);
        
        // 로그인 성공 여부를 확인합니다.
        if (loginSeller != null) {
        	// 로그인 성공 시, 세션에 'loginUser' 키로 판매자 로그인 정보를 저장합니다.
            // (참고: 일반/판매자 로그인이 같은 "loginUser" 키를 사용합니다.)
            session.setAttribute("loginUser", loginSeller);
            // 판매자 메인 대시보드 URL로 리다이렉트합니다.
            return "redirect:/seller"; 
        }
        
        // 로그인 실패 시, 에러 메시지를 Flash Attribute에 추가하여 리다이렉트 후에도 메시지가 표시되도록 합니다.
        redirectAttributes.addFlashAttribute("errorMessage", "아이디 또는 비밀번호가 일치하지 않습니다.");
        // 로그인 페이지로 다시 리다이렉트합니다.
        return "redirect:/seller";
    }

}
