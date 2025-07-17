package ks55team02.admin.login.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import ks55team02.admin.login.domain.AdminLoginRequest;
import ks55team02.admin.login.service.AdminService;
import ks55team02.customer.login.domain.LoginUser;
import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/adminpage") // 이 컨트롤러의 모든 URL은 /adminpage로 시작합니다.
@RequiredArgsConstructor
public class AdminLoginController {

	private final AdminService adminService;

    @GetMapping("/login")
    public String adminLoginPage() {
        return "admin/login/adminLogin";
    }

    @PostMapping("/login")
    public String adminLoginProcess(
            AdminLoginRequest loginRequest,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // 5. 새로 만든 adminService의 login 메소드를 호출
        LoginUser loginAdmin = adminService.login(loginRequest);
        
        if (loginAdmin != null) {
            // [핵심 수정] 세션에 저장하는 키 값을 "loginAdmin"에서 "loginUser"로 변경합니다.
            session.setAttribute("loginUser", loginAdmin); 
            return "redirect:/adminpage";
        }
        
        redirectAttributes.addFlashAttribute("errorMessage", "아이디 또는 비밀번호가 일치하지 않습니다.");
        return "redirect:/adminpage/login";
    }
    /**
     * 관리자 로그아웃을 처리합니다.
     * 세션을 무효화하고 관리자 로그인 페이지로 리다이렉트합니다.
     * 
     * @param request HttpServletRequest 객체, 세션을 가져오기 위해 사용됩니다.
     * @return 관리자 로그인 페이지(/adminpage/login)로의 리다이렉트 경로
     */
    @GetMapping("/logout")
    public String adminLogout(HttpServletRequest request) {
        
        // 현재 세션이 존재하면 가져오고, 없으면 null을 반환합니다.
        HttpSession session = request.getSession(false);
        
        // 세션이 존재하는 경우에만 무효화(invalidate)를 실행하여 안전하게 처리합니다.
        if (session != null) {
            session.invalidate();
        }
        
        // 로그아웃 후에는 관리자 로그인 페이지로 이동시킵니다.
        return "redirect:/adminpage/login";
    }
}
