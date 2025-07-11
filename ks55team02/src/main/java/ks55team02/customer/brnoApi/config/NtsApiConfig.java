package ks55team02.customer.brnoApi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Configuration // 이 클래스가 Spring의 설정 클래스임을 나타냅니다.
@Slf4j
public class NtsApiConfig {

    /**
     * WebClient 빈을 정의하여 NtsApiService에 주입될 수 있도록 합니다.
     * 여기에 baseUrl을 설정하여 NtsApiService 내에서 매번 API_BASE_URL을 사용하지 않도록 합니다.
     *
     * @param webClientBuilder Spring이 자동으로 주입해주는 WebClient.Builder
     * @return 국세청 API 호출을 위한 설정이 완료된 WebClient 인스턴스
     */
    @Bean("ntsWebClient")
    public WebClient ntsWebClient(WebClient.Builder webClientBuilder) {
    	DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory("https://api.odcloud.kr/api/nts-businessman/v1");
    	factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        return webClientBuilder
                // 국세청 API의 기본 URL을 설정합니다.
                // 이 URL은 모든 국세청 API 호출의 시작점이 됩니다.
                .baseUrl("https://api.odcloud.kr/api/nts-businessman/v1")
                .uriBuilderFactory(factory)
                // 필요에 따라 기본 헤더, 타임아웃 등을 여기서 설정할 수 있습니다.
                // .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                // .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                // 요청/응답 로깅 필터 추가
                .filter(logRequestAndResponse())
                .build();
    }
    // 요청후 확인하는 필터
    private ExchangeFilterFunction logRequestAndResponse() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            // 요청 헤더, 메서드, URL, 본문 등을 여기서 로깅
            log.info("WebClient Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> log.info("  {}: {}", name, values));
            // 본문 로깅은 Flux/Mono의 소비를 유발하므로 주의가 필요합니다.
            // .bodyToMono(String.class)를 사용하면 스트림이 소진되어 실제 요청에 문제가 생길 수 있습니다.
            // 디버깅 목적으로만 짧게 사용하거나, BodyExtractors.toDataBuffers()를 사용해야 합니다.
            // 간단히는 요청 DTO를 서비스 레이어에서 로그하는 것이 더 안전합니다.
            return Mono.just(clientRequest);
        }).andThen(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            // 응답 상태 코드, 헤더 등을 여기서 로깅
        	log.info("WebClient Response Status: {}", clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders().forEach((name, values) -> log.info("  {}: {}", name, values));
            return Mono.just(clientResponse);
        }));
    }
}
