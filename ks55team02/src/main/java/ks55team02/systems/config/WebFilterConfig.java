package ks55team02.systems.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class WebFilterConfig implements Filter{

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		
		String uri = httpRequest.getRequestURI();
	
		// 크롬 개발자 도구 자동 요청이나 인증 경로 무시
		if (uri.contains(".well-known") || uri.contains("com.chrome.devtools.json")) {
			httpResponse.setStatus(HttpServletResponse.SC_OK);
			httpResponse.setContentType("applecation/json");
			httpResponse.setCharacterEncoding("UTF-8");
			httpResponse.getWriter().write("{\"status\": \"ok\", \"mesaage\": \"Chrome DevTools auto request ignored\"}");
			return;
		}
		
		// 나머지는 정상 흐름대로 진행
		chain.doFilter(request, response);
	}
}
