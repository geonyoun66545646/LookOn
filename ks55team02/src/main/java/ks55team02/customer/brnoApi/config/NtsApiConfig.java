package ks55team02.customer.brnoApi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration // 이 클래스가 Spring의 설정 클래스임을 나타냅니다.
public class NtsApiConfig {

    /**
     * WebClient 빈을 정의하여 NtsApiService에 주입될 수 있도록 합니다.
     * 여기에 baseUrl을 설정하여 NtsApiService 내에서 매번 API_BASE_URL을 사용하지 않도록 합니다.
     *
     * @param webClientBuilder Spring이 자동으로 주입해주는 WebClient.Builder
     * @return 국세청 API 호출을 위한 설정이 완료된 WebClient 인스턴스
     */
    @Bean
    public WebClient ntsWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                // 국세청 API의 기본 URL을 설정합니다.
                // 이 URL은 모든 국세청 API 호출의 시작점이 됩니다.
                .baseUrl("https://api.odcloud.kr/api/nts-businessman/v1")
                // 필요에 따라 기본 헤더, 타임아웃 등을 여기서 설정할 수 있습니다.
                // .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                // .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
