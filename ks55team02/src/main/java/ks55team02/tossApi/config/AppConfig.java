package ks55team02.tossApi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 프로젝트 전역에서 사용될 객체(Bean)들을 정의하는 설정 클래스
 */
@Configuration // 이 클래스가 스프링의 '설정 파일'임을 알려주는 어노테이션입니다.
public class AppConfig {

    /**
     * RestTemplate 객체를 생성하여 Spring 컨테이너에 Bean으로 등록합니다.
     * 이제 다른 클래스에서 @Autowired나 생성자 주입을 통해 RestTemplate을 사용할 수 있습니다.
     * @return 프로젝트 전역에서 사용될 RestTemplate 객체
     */
    @Bean // 이 메소드가 반환하는 객체를 Spring Bean으로 등록하라는 의미의 어노테이션입니다.
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
