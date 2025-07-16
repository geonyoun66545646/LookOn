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

@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerLoginController {

	private final SellerLoginService sellerLoginService;

    /**
     * 판매자 로그인 페이지 화면을 보여주는 메소드
     */
    @GetMapping("/login")
    public String sellerLoginPage() {
        // templates/seller/login/sellerLogin.html 파일을 찾아서 보여줍니다.
        return "seller/login/sellerLogin";
    }

    /**
     * 판매자 로그인 폼에서 전송된 데이터를 처리하는 메소드
     */
    @PostMapping("/login")
    public String sellerLoginProcess(
            SellerLoginRequest loginRequest,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        LoginUser loginSeller = sellerLoginService.login(loginRequest);
        
        if (loginSeller != null) {
            // 일반/판매자 로그인은 같은 "loginUser" 키를 사용합니다.
            session.setAttribute("loginUser", loginSeller);
            // 판매자 메인 대시보드로 리다이렉트합니다.
            return "redirect:/seller"; 
        }
        
        redirectAttributes.addFlashAttribute("errorMessage", "아이디 또는 비밀번호가 일치하지 않습니다.");
        return "redirect:/seller";
    }

}
