package ks55team02.systems.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class NoCacheInterceptor implements HandlerInterceptor {

	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // 브라우저가 페이지를 캐싱하지 않도록 응답 헤더를 설정합니다.
        // HTTP 1.1 표준
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        
        // HTTP 1.0 하위 호환성을 위함
        response.setHeader("Pragma", "no-cache");
        
        // 프록시 서버의 캐싱 방지
        response.setDateHeader("Expires", 0);
        
        // 이 인터셉터는 요청의 흐름을 막지 않으므로 항상 true를 반환하여 계속 진행시킵니다.
        return true;
    }
}
