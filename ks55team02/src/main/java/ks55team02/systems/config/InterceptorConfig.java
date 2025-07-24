package ks55team02.systems.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import ks55team02.systems.interceptor.AdminCheckInterceptor;
import ks55team02.systems.interceptor.LogInterceptor;
import ks55team02.systems.interceptor.LoginAndAdminIsolateInterceptor;
import ks55team02.systems.interceptor.NoCacheInterceptor;
import ks55team02.systems.interceptor.SellerCheckInterceptor;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer{

	private final AdminCheckInterceptor adminCheckInterceptor;
    private final LoginAndAdminIsolateInterceptor loginAndAdminIsolateInterceptor; // 1. 필드 선언
    private final NoCacheInterceptor noCacheInterceptor;
    private final SellerCheckInterceptor sellerCheckInterceptor;
    private final LogInterceptor logInterceptor;
    
    @Value("${project.interceptors.enabled}")
    private boolean interceptorsEnabled;
    
    // 3. 생성자에서 NoCacheInterceptor도 주입받도록 수정
    public InterceptorConfig(AdminCheckInterceptor adminCheckInterceptor, 
                             LoginAndAdminIsolateInterceptor loginAndAdminIsolateInterceptor,
                             NoCacheInterceptor noCacheInterceptor,
                             SellerCheckInterceptor sellerCheckInterceptor,
                             LogInterceptor logInterceptor) {
        this.adminCheckInterceptor = adminCheckInterceptor;
        this.loginAndAdminIsolateInterceptor = loginAndAdminIsolateInterceptor;
        this.noCacheInterceptor = noCacheInterceptor;
        this.sellerCheckInterceptor = sellerCheckInterceptor;
        this.logInterceptor = logInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	
    	// access 이력 조회
    	registry.addInterceptor(logInterceptor)
    			.addPathPatterns("/**")
    			.excludePathPatterns("/admincss/**")
    			.excludePathPatterns("/favicons/**")
    			.excludePathPatterns("/git/**")
    			.excludePathPatterns("/js/**")
    			.excludePathPatterns("/attachment/**")
    			.excludePathPatterns("/maincss/**")
    			.excludePathPatterns("/uploads/**");
    	
    	if (interceptorsEnabled) {
    	
    	// [신규] 0순위: 관리자 페이지 캐시 방지 인터셉터
        registry.addInterceptor(noCacheInterceptor)
                .order(0) // 가장 먼저 실행되어 응답 헤더를 설정
                .addPathPatterns("/adminpage/**"); // 관리자 페이지에만 적용
        
        // 관리자 페이지 접근 제어 인터셉터
        registry.addInterceptor(adminCheckInterceptor)
                .order(1)
                .addPathPatterns("/adminpage/**")
                .excludePathPatterns("/adminpage/login");

        // 관리자 격리 인터셉터
        registry.addInterceptor(loginAndAdminIsolateInterceptor)
                .order(2)
                .addPathPatterns("/**")
                .excludePathPatterns(
                		// --- 기본 페이지 및 로그인/아웃 ---
                        "/login", "/logout",

                        // --- 모든 리소스 폴더 ---
                        "/admincss/assets/**", 

                        // --- 인터셉터가 관여하지 않을 페이지 영역 ---
                        "/adminpage/**", // 관리자 페이지 영역
                        "/seller/**"     // 판매자 페이지 영역
                );
     // [신규] 3순위: 판매자 페이지 접근 제어 인터셉터
        registry.addInterceptor(sellerCheckInterceptor)
                .order(3) // 다른 인터셉터와 겹치지 않는 순서로 설정
                .addPathPatterns("/seller/**") // '/seller/'로 시작하는 모든 경로에 적용
                .excludePathPatterns("/seller/login"); // 추후 판매자 전용 로그인 페이지가 생길 경우를 대비해 제외
    
    	}
    }
}
