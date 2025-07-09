package ks55team02.customer.login.config;

import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor{

	/**
     * preHandle: Controller 실행 전에 동작하는 메소드
     * - 반환 값이 true일 경우: 요청한 Controller를 계속 실행
     * - 반환 값이 false일 경우: 요청한 Controller 실행을 중단
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();
        log.info("인증 체크 인터셉터 실행: {}", requestURI);

        // 1. 현재 세션을 가져온다. (세션이 없으면 null 반환)
        HttpSession session = request.getSession(false);

        // 2. 세션이 없거나, 세션에 "loginUser" 정보가 없으면 로그인하지 않은 것으로 판단
        if (session == null || session.getAttribute("loginUser") == null) {
            log.info("미인증 사용자 요청. 로그인 페이지로 리다이렉트합니다.");

            // 3. 리다이렉트 후에도 "로그인이 필요한 서비스입니다." 알림을 띄우기 위해 세션에 메시지 저장
            //    메시지를 담아야 하므로, 세션이 없다면 새로 생성한다.
            session = request.getSession();
            session.setAttribute("authAlert", "로그인이 필요한 서비스입니다.");
            
            // 4. 원래 가려던 주소를 세션에 저장 (로그인 성공 후 그 페이지로 바로 이동시키기 위함)
            session.setAttribute("redirectUrlAfterLogin", requestURI);
            
            // 5. 우리 프로젝트의 메인 페이지로 리다이렉트 시킨다.
            response.sendRedirect("/main");

            return false; // 컨트롤러 실행을 막고 여기서 요청 처리 종료
        }

        // 6. 세션에 "loginUser" 정보가 있다면, 로그인 된 것이므로 요청을 그대로 진행시킨다.
        return true;
    }
}
