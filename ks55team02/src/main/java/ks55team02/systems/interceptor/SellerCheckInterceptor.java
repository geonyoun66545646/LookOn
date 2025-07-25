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
public class SellerCheckInterceptor implements HandlerInterceptor {

	private static final Logger log = LoggerFactory.getLogger(SellerCheckInterceptor.class);

    // 판매자 등급 코드를 상수로 정의하여 가독성 및 유지보수성 향상
    private static final String SELLER_GRADE = "grd_cd_2";
    
    // 관리자 등급 코드 목록 (AdminCheckInterceptor의 대상이지만, SellerCheckInterceptor에서 예외 처리할 때 사용)
    private static final List<String> ADMIN_GRADES = Arrays.asList("grd_cd_0", "grd_cd_1"); // 관리자 등급을 여기에 정의

    // 관리자도 접근을 허용할 특정 판매자 페이지 패턴 정의
    private static final List<String> ADMIN_ALLOWED_SELLER_PATHS = Arrays.asList(
        "/seller/products/preview/"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (requestURI.startsWith(contextPath)) {
            requestURI = requestURI.substring(contextPath.length());
        }

        // 1. 로그인 상태 확인
        if (session == null || session.getAttribute("loginUser") == null) {
            log.warn("비로그인 사용자의 판매자 페이지 접근 시도. URI: {}", requestURI);
            response.sendRedirect(request.getContextPath() + "/login"); // 로그인 페이지로 리다이렉트
            return false;
        }

        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        String gradeCode = loginUser.getMbrGrdCd();

        // 2. 관리자 등급인지 확인 및 예외 처리
        if (ADMIN_GRADES.contains(gradeCode)) {
            boolean isAllowedSellerPathForAdmin = false;
            for (String allowedPath : ADMIN_ALLOWED_SELLER_PATHS) {
                if (requestURI.startsWith(allowedPath)) {
                    isAllowedSellerPathForAdmin = true;
                    break;
                }
            }

            if (isAllowedSellerPathForAdmin) {
                log.info("관리자(ID: {}, Grade: {})의 허용된 판매자 페이지 접근. URI: {}",
                         loginUser.getUserLgnId(), gradeCode, requestURI);
                return true; // 관리자이면서 허용된 판매자 페이지이므로 통과
            } else {
                // 관리자이지만 이 판매자 페이지는 허용되지 않음 (판매자 페이지의 다른 부분)
                log.warn("관리자(ID: {}, Grade: {})의 권한 없는 판매자 페이지 접근 시도. 관리자 홈으로 리다이렉트. URI: {}",
                         loginUser.getUserLgnId(), gradeCode, requestURI);
                response.sendRedirect(request.getContextPath() + "/adminpage/dashboard"); // 관리자 홈으로 리다이렉트
                return false;
            }
        }

        // 3. 판매자 등급인지 확인 (본래 SellerCheckInterceptor의 역할)
        if (SELLER_GRADE.contains(gradeCode)) {
            log.info("판매자(ID: {}, Grade: {})의 판매자 페이지 접근 허용. URI: {}",
                     loginUser.getUserLgnId(), gradeCode, requestURI);
            return true; // 판매자이므로 통과
        } else {
            // 판매자도 관리자도 아닌데 판매자 페이지 접근 시도
            log.warn("권한 없는 사용자(ID: {}, Grade: {})의 판매자 페이지 접근 시도. 로그인 페이지로 리다이렉트. URI: {}",
                     loginUser.getUserLgnId(), gradeCode, requestURI);
            response.sendRedirect(request.getContextPath() + "/login"); // 또는 권한 없음 페이지 등
            return false;
        }
    }
}
