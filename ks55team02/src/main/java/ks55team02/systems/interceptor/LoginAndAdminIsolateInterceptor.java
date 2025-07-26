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
public class LoginAndAdminIsolateInterceptor implements HandlerInterceptor {

	private static final Logger log = LoggerFactory.getLogger(LoginAndAdminIsolateInterceptor.class);
	
	private static final List<String> ADMIN_GRADES = Arrays.asList("grd_cd_0", "grd_cd_1");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        HttpSession session = request.getSession(false);

        // 세션이 존재하고, 로그인 정보가 있는 경우에만 검사
        if (session != null && session.getAttribute("loginUser") != null) {
            LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
            String gradeCode = loginUser.getMbrGrdCd();

            // [핵심 로직] 로그인한 사용자의 등급이 '관리자'인 경우
            if (ADMIN_GRADES.contains(gradeCode)) {
                log.info("관리자(ID: {}, Grade: {}) 외부 페이지 접근 차단. 관리자 홈으로 리다이렉트. URI: {}", 
                         loginUser.getUserLgnId(), gradeCode, request.getRequestURI());
                
                response.sendRedirect(request.getContextPath() + "/adminpage/dashboard");
                return false;
            }
        }

        // TODO: 향후 여기에 일반 사용자의 로그인 체크 로직을 추가할 수 있습니다.
        // 예: if (session == null) { response.sendRedirect("/login"); return false; }

        // 관리자가 아니거나, 비로그인 상태면 요청을 그대로 통과시킴
        // (이 인터셉터의 현재 목표는 오직 '관리자 격리'이므로)
        return true;
    }
}
