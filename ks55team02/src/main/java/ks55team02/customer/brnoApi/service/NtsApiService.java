package ks55team02.customer.brnoApi.service;

import java.net.URLDecoder;
import java.net.URLEncoder; // URLEncoder 클래스 임포트
import java.nio.charset.StandardCharsets; // StandardCharsets 클래스 임포트
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ks55team02.customer.brnoApi.domain.NtsApiRequestDto;
import ks55team02.customer.brnoApi.domain.NtsApiResponseDto;
import reactor.core.publisher.Mono;

@Service // 이 클래스가 Spring의 서비스 컴포넌트임을 나타냅니다.
public class NtsApiService {

    // 로깅을 위한 Logger 인스턴스를 생성합니다.
    private static final Logger logger = LoggerFactory.getLogger(NtsApiService.class);

    // application.properties 파일에서 "api.nts.service-key" 값을 주입받습니다.
    @Value("${api.nts.service-key}")
    private String serviceKey;

    // 국세청 API의 사업자등록 상태 조회 엔드포인트 경로
    private final String STATUS_ENDPOINT = "/status";

    private final WebClient webClient; // WebClient 인스턴스 (NtsApiConfig에서 정의된 빈을 주입받습니다)
    private final ObjectMapper objectMapper; // JSON 직렬화/역직렬화를 위한 ObjectMapper

    /**
     * NtsApiService의 생성자입니다.
     * Spring이 @Qualifier("ntsWebClient")를 통해 NtsApiConfig에서 정의된 WebClient 빈을 주입하고,
     * ObjectMapper를 자동으로 주입합니다.
     *
     * @param webClient WebClient 인스턴스
     * @param objectMapper ObjectMapper 인스턴스
     */
    public NtsApiService(@Qualifier("ntsWebClient") WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 사업자등록 상태(휴폐업)를 국세청 API를 통해 비동기적으로 조회합니다.
     * 이 API는 POST 방식으로 호출되며, serviceKey는 URL 쿼리 파라미터로,
     * 사업자등록번호는 JSON 요청 본문으로 전달됩니다.
     *
     * @param brno 조회할 사업자등록번호 (하이픈 없이 숫자 10자리)
     * @return NtsApiResponseDto를 담은 Mono 객체 (비동기 처리 결과)
     */
    public Mono<NtsApiResponseDto> checkBusinessStatus(String brno) {
        // 국세청 API 요청 본문에 필요한 DTO를 생성합니다.
        // Collections.singletonList를 사용하여 단일 사업자번호를 리스트로 만듭니다.
        NtsApiRequestDto requestBody = new NtsApiRequestDto(Collections.singletonList(brno));

        // *** 중요: 서비스 키를 URL에 안전하게 포함시키기 위해 URLEncoder를 사용하여 인코딩합니다. ***
        // 특히 서비스 키에 +, /, = 등의 특수문자가 포함될 경우 이 과정이 필수적입니다.
        String encodedServiceKey;
        try {
            encodedServiceKey = URLEncoder.encode("F+q3vYQQ5IfWC/sf7bavxmaBbjXdFXl80vQFZMfYGlEsLDUdYAIkfHmW4XkD4s/iJjS6Yc/xP6WTECH4Uw2SDA==", StandardCharsets.UTF_8.toString());
            logger.info("인코딩된 서비스 키: {}", encodedServiceKey); // 디버깅을 위해 인코딩된 키를 로그로 출력
        } catch (Exception e) {
            // 인코딩 중 오류가 발생하면 로그를 남기고 RuntimeException을 Mono.error로 반환합니다.
            logger.error("서비스 키 인코딩 중 오류 발생: {}", serviceKey, e);
            return Mono.error(new RuntimeException("서비스 키 인코딩 오류: " + e.getMessage(), e));
        }

        // WebClient를 사용하여 국세청 API에 POST 요청을 보냅니다.
        return webClient.post() // HTTP POST 메소드 지정
                .uri(uriBuilder -> uriBuilder
                        .path(STATUS_ENDPOINT) // "/status" 엔드포인트 경로 지정
                        .queryParam("serviceKey", "F%2Bq3vYQQ5IfWC%2Fsf7bavxmaBbjXdFXl80vQFZMfYGlEsLDUdYAIkfHmW4XkD4s%2FiJjS6Yc%2FxP6WTECH4Uw2SDA%3D%3D") // 인코딩된 서비스 키를 쿼리 파라미터로 추가
                        .build()) // URI 빌드 완료
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)       // 응답은 JSON으로 받겠다고 명시
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) // 요청 본문은 JSON임을 명시
                .header(HttpHeaders.AUTHORIZATION, "*/*")
                .bodyValue(requestBody) // 요청 본문으로 NtsApiRequestDto 객체를 전달
                .retrieve() // 응답을 가져옵니다.
                // HTTP 상태 코드가 에러(4xx, 5xx)인 경우 처리 로직을 정의합니다.
                .onStatus(HttpStatusCode::isError, // HttpStatusCode::isError는 4xx 또는 5xx 범위의 상태 코드를 감지합니다.
                          (ClientResponse clientResponse) -> // 에러 응답 객체를 받습니다.
                    clientResponse.bodyToMono(String.class) // 에러 응답 본문을 문자열로 읽습니다.
                                  .flatMap(errorBody -> {
                                      // 에러 로그를 기록하고 RuntimeException으로 변환하여 Mono.error를 반환합니다.
                                      logger.error("국세청 API 호출 실패 (HTTP {}): {}", clientResponse.statusCode().value(), errorBody);
                                      return Mono.error(new RuntimeException(
                                          "API 호출 실패: " + clientResponse.statusCode().value() + ", 응답 본문: " + errorBody
                                      ));
                                  }))
                .bodyToMono(String.class) // 성공적인 응답 본문을 문자열(Raw JSON)로 받습니다.
                .doOnNext(jsonResponse -> logger.info("국세청 API 실제 응답 (Raw JSON): {}", jsonResponse)) // 실제 JSON 응답을 로그로 출력 (디버깅용)
                .flatMap(jsonResponse -> {
                    try {
                        // 수신된 JSON 문자열을 NtsApiResponseDto 객체로 역직렬화(파싱)합니다.
                        return Mono.just(objectMapper.readValue(jsonResponse, NtsApiResponseDto.class));
                    } catch (JsonProcessingException e) {
                        // JSON 파싱 중 오류가 발생하면 로그를 남기고 예외를 반환합니다.
                        logger.error("JSON 파싱 중 오류 발생: {}", jsonResponse, e);
                        return Mono.error(new RuntimeException("JSON 파싱 오류: " + e.getMessage(), e));
                    }
                })
                .doOnError(e -> logger.error("API 호출 최종 처리 중 예외 발생", e)); // API 호출 및 처리 과정에서 발생한 최종 예외를 로그로 기록
    }
}