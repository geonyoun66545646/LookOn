package ks55team02.admin.login.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            session.setAttribute("loginAdmin", loginAdmin);
            return "redirect:/adminpage";
        }
        
        redirectAttributes.addFlashAttribute("errorMessage", "아이디 또는 비밀번호가 일치하지 않습니다.");
        return "redirect:/adminpage/login";
    }
    
    // 로그인 성공 후 이동할 임시 대시보드 페이지
    @GetMapping("/adminpage")
    public String adminDashboard() {
        return "admin/main"; 
    }
}
