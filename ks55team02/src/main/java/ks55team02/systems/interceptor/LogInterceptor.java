package ks55team02.systems.interceptor;

import java.util.Set;
import java.util.StringJoiner;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LogInterceptor implements HandlerInterceptor{

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// 사용자 요청 파라미터 ?memberId=id1&memberPw=pw1 -> memberId, memberPw
		// 용청 파라미터 키 추출
		Set<String> paramKey = request.getParameterMap().keySet();
		
		StringJoiner param = new StringJoiner(", ");
		
		// memberId=id1, memberPw=pw1, .....
		for(String key : paramKey) {
			param.add(key + ": " + request.getParameter(key));
		}
		
		String ip = request.getHeader("X-Real-IP");
		ip = (ip != null) ? ip : request.getRemoteAddr();
		
		log.info("========================== Access LOG START =========================");
		log.info("PORT		:::: 	{}", request.getLocalPort());
		log.info("ServerName	:::: 	{}", request.getServerName());
		log.info("Method		:::: 	{}", request.getMethod());
		log.info("RequestURI	:::: 	{}", request.getRequestURI());
		log.info("CLIENT IP	:::: 	{}", ip);
		if(param.length() > 0) {
			log.info("PARAMETER	:::: 	{}", param);			
		}
		log.info("========================== Access LOG END ==========================");
		
		return HandlerInterceptor.super.preHandle(request, response, handler);
	}
}
