package ks55team02.customer.login.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer{

	 @Override
	    public void addInterceptors(InterceptorRegistry registry) {
	        // 우리가 만든 로그인 체크 인터셉터를 등록합니다.
	        registry.addInterceptor(new LoginCheckInterceptor())
	                .order(1) // 인터셉터 체인에서 첫 번째로 실행되도록 순서 지정
	                
	                // 인터셉터를 적용할 URL 패턴을 지정합니다. (로그인이 필요한 모든 경로)
	                .addPathPatterns(
	                    "/customer/mypage/**",  // 마이페이지 및 그 하위 모든 경로
	                    "/customer/order/**"   // 주문 관련 모든 경로 (예시)
	                    // 여기에 로그인이 필요한 다른 URL 패턴들을 계속 추가하면 됩니다.
	                );
	                
	                // 인터셉터를 적용하지 않을 URL 패턴 (예외 처리)
	                // 로그인/로그아웃 자체는 검사하면 안되므로 제외합니다.
	                // 회원가입 관련 경로도 당연히 제외해야 합니다.
	                // .excludePathPatterns()를 사용해도 되지만, 
	                // .addPathPatterns()에 명시되지 않은 경로는 어차피 검사하지 않으므로
	                // 여기서는 생략해도 무방합니다.
	    }
}
