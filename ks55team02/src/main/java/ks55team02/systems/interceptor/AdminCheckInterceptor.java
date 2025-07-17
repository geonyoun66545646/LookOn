package ks55team02.systems.interceptor;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ks55team02.customer.login.domain.LoginUser;

@Component
public class AdminCheckInterceptor implements HandlerInterceptor{

	private static final Logger log = LoggerFactory.getLogger(AdminCheckInterceptor.class);
	
	private static final List<String> ADMIN_GRADES = Arrays.asList("grd_cd_0", "grd_cd_1");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("loginUser") == null) {
            log.info("비로그인 사용자 접근 시도. 관리자 로그인 페이지로 리다이렉트. URI: {}", request.getRequestURI());
            response.sendRedirect(request.getContextPath() + "/adminpage/login");
            return false;
        }

        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        
        String gradeCode = loginUser.getMbrGrdCd(); 

        if (gradeCode == null || !ADMIN_GRADES.contains(gradeCode)) {
            log.warn("권한 없는 사용자(Grade: {})의 관리자 페이지 접근 시도. URI: {}", gradeCode, request.getRequestURI());
            response.sendRedirect(request.getContextPath() + "/");
            return false;
        }

        log.info("관리자(ID: {}) 접근 허용. URI: {}", loginUser.getUserLgnId(), request.getRequestURI());
        return true;
    }
}
