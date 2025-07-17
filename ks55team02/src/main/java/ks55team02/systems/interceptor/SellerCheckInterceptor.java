package ks55team02.systems.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ks55team02.customer.login.domain.LoginUser;

@Component
public class SellerCheckInterceptor implements HandlerInterceptor {

	private static final Logger log = LoggerFactory.getLogger(SellerCheckInterceptor.class);

    // 판매자 등급 코드를 상수로 정의하여 가독성 및 유지보수성 향상
    private static final String SELLER_GRADE = "grd_cd_2";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        HttpSession session = request.getSession(false);

        // 1. 비로그인 사용자의 경우
        if (session == null || session.getAttribute("loginUser") == null) {
            log.info("비로그인 사용자의 판매자 페이지 접근 시도. 로그인 페이지로 리다이렉트. URI: {}", request.getRequestURI());
            
            // 로그인 후 원래 보려던 페이지로 돌아갈 수 있도록 현재 요청 경로를 쿼리 파라미터로 전달
            String redirectURL = request.getRequestURI();
            response.sendRedirect(request.getContextPath() + "/login?redirectURL=" + redirectURL);
            return false; // 요청 중단
        }

        // 2. 로그인은 했지만 권한이 없는 사용자의 경우 (관리자, 일반 사용자 등)
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        String gradeCode = loginUser.getMbrGrdCd();

        // 사용자의 등급 코드가 판매자 등급과 일치하지 않는 경우
        if (!SELLER_GRADE.equals(gradeCode)) {
            log.warn("권한 없는 사용자(Grade: {})의 판매자 페이지 접근 시도. URI: {}", gradeCode, request.getRequestURI());
            
            // 권한이 없다는 것을 인지할 수 있도록 메인 페이지로 보냄
            response.sendRedirect(request.getContextPath() + "/");
            return false; // 요청 중단
        }
        
        // 3. 모든 검사를 통과한 경우 (로그인한 판매자)
        log.info("판매자(ID: {}) 접근 허용. URI: {}", loginUser.getUserLgnId(), request.getRequestURI());
        return true; // 요청 계속 진행
    }
}
