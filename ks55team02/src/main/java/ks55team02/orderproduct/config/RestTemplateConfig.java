package ks55team02.orderproduct.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// 배송 추적 config
@Configuration // 이 클래스가 스프링 설정 클래스임을 나타냅니다.
public class RestTemplateConfig {
	
    @Bean // 이 메소드가 반환하는 객체를 스프링 빈으로 등록합니다.
    public RestTemplate restTemplate() {
    	
        // RestTemplate 인스턴스를 생성하고 반환합니다.
        // 필요에 따라 여기에 HttpComponentsClientHttpRequestFactory 등을 설정하여
        // 타임아웃, 커넥션 풀 등의 고급 설정을 할 수 있습니다.
        return new RestTemplate();
    }
}
