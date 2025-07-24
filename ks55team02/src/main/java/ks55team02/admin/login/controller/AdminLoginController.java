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
import ks55team02.customer.login.domain.LoginUser; // 고객 도메인의 LoginUser를 사용
import lombok.RequiredArgsConstructor;

/**
 * 관리자 로그인 관련 요청을 처리하는 컨트롤러 클래스입니다.
 * 관리자 로그인 페이지 표시, 로그인 처리 및 로그아웃 기능을 담당합니다.
 */
@Controller
@RequestMapping("/adminpage") // 이 컨트롤러의 모든 URL은 '/adminpage'로 시작합니다.
@RequiredArgsConstructor // Lombok을 사용하여 final 필드를 사용하는 생성자를 자동으로 생성합니다.
public class AdminLoginController {

	private final AdminService adminService; // 관리자 로그인 비즈니스 로직을 처리하는 서비스

    /**
     * 관리자 로그인 페이지 화면을 제공하는 GET 요청 핸들러 메소드입니다.
     *
     * @return 관리자 로그인 페이지의 뷰 경로 (templates/admin/login/adminLogin.html)
     */
    @GetMapping("/login")
    public String adminLoginPage() {
        return "admin/login/adminLogin";
    }

    /**
     * 관리자 로그인 폼에서 전송된 데이터를 처리하는 POST 요청 핸들러 메소드입니다.
     * 로그인 요청을 받아 인증 처리 후 성공/실패에 따라 리다이렉트합니다.
     *
     * @param loginRequest     로그인 폼에서 전송된 아이디, 비밀번호 등을 담은 {@link AdminLoginRequest} DTO
     * @param session          현재 사용자의 HTTP 세션 객체 (로그인 정보 저장에 사용)
     * @param redirectAttributes 리다이렉트 시 일회성 메시지(Flash Attribute)를 전달하기 위한 객체
     * @return                 로그인 성공 시 관리자 대시보드로, 실패 시 로그인 페이지로 리다이렉트하는 뷰 경로
     */
    @PostMapping("/login")
    public String adminLoginProcess(
            AdminLoginRequest loginRequest,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // 5. adminService의 login 메소드를 호출하여 관리자 로그인 로직을 수행합니다.
        LoginUser loginAdmin = adminService.login(loginRequest);
        
        // 로그인 성공 여부를 확인합니다.
        if (loginAdmin != null) {
            // [핵심 수정] 세션에 저장하는 키 값을 "loginAdmin"에서 "loginUser"로 변경합니다.
            // 이는 고객/판매자/관리자 모두 동일한 LoginUser 도메인 객체를 사용하며,
            // 세션 키를 통일하여 접근성을 높일 수 있습니다.
            session.setAttribute("loginUser", loginAdmin); 
            // 로그인 성공 시 관리자 대시보드 URL로 리다이렉트합니다.
            return "redirect:/adminpage/dashboard";
        }
        
        // 로그인 실패 시, 에러 메시지를 Flash Attribute에 추가하여 리다이렉트 후에도 메시지가 표시되도록 합니다.
        redirectAttributes.addFlashAttribute("errorMessage", "아이디 또는 비밀번호가 일치하지 않습니다.");
        // 로그인 페이지로 다시 리다이렉트합니다.
        return "redirect:/adminpage/login";
    }

    /**
     * 관리자 로그아웃을 처리하는 GET 요청 핸들러 메소드입니다.
     * 현재 관리자 세션을 무효화하고 관리자 로그인 페이지로 리다이렉트합니다.
     *
     * @param request HttpServletRequest 객체, 세션을 가져오기 위해 사용됩니다.
     * @return        관리자 로그인 페이지(/adminpage/login)로의 리다이렉트 경로
     */
    @GetMapping("/logout")
    public String adminLogout(HttpServletRequest request) {
        
        // 현재 세션이 존재하는지 확인하고, 존재하면 가져오고 없으면 null을 반환합니다.
        HttpSession session = request.getSession(false);
        
        // 세션이 존재하는 경우에만 무효화(invalidate)를 실행하여 세션 데이터를 안전하게 삭제합니다.
        if (session != null) {
            session.invalidate();
        }
        
        // 로그아웃 처리 후에는 관리자 로그인 페이지로 이동시킵니다.
        return "redirect:/adminpage/login";
    }
}