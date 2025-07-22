package ks55team02.customer.brnoapi.controller;

import org.springframework.web.bind.annotation.GetMapping; // GET 요청 매핑을 위한 어노테이션
import org.springframework.web.bind.annotation.RequestMapping; // 요청 경로 매핑을 위한 어노테이션
import org.springframework.web.bind.annotation.RequestParam; // 쿼리 파라미터 바인딩을 위한 어노테이션
import org.springframework.web.bind.annotation.RestController; // REST 컨트롤러임을 명시하는 어노테이션

import ks55team02.customer.brnoapi.domain.NtsApiResponseDto;
import ks55team02.customer.brnoapi.service.NtsApiService;
import reactor.core.publisher.Mono; // 비동기 처리를 위한 Mono 임포트

@RestController // 이 클래스가 RESTful 웹 서비스를 제공하는 컨트롤러임을 명시합니다.
@RequestMapping("/api/brno") // 이 컨트롤러의 모든 메서드의 기본 경로를 /api/brno로 설정합니다.
public class BrnoApiController {

    private final NtsApiService ntsApiService; // NtsApiService를 주입받기 위한 필드

    /**
     * BrnoApiController의 생성자입니다.
     * Spring이 NtsApiService 빈을 자동으로 주입해줍니다 (생성자 주입 방식).
     *
     * @param ntsApiService 주입받을 NtsApiService 인스턴스
     */
    public BrnoApiController(NtsApiService ntsApiService) {
        this.ntsApiService = ntsApiService;
    }

    /**
     * 사업자등록번호의 휴폐업 상태를 조회하는 API 엔드포인트입니다.
     * 클라이언트로부터 GET 요청을 받아 사업자등록번호를 쿼리 파라미터로 받습니다.
     * 최종 호출 경로: /api/brno/status?brno={사업자등록번호}
     *
     * @param brno 조회할 사업자등록번호 (하이픈 없이 10자리 숫자)
     * @return Mono<NtsApiResponseDto> - 국세청 API 응답 데이터를 포함하는 Mono 객체 (비동기 결과)
     */
    @GetMapping("/status") // GET 요청에 대해 /status 경로를 매핑합니다.
    public Mono<NtsApiResponseDto> getBusinessStatus(@RequestParam("brno") String brno) {
        // NtsApiService를 통해 실제 국세청 API를 호출하고 그 비동기 결과를 반환합니다.
        // NtsApiService 내부에서는 국세청 API에 POST 요청을 보냅니다.
        return ntsApiService.checkBusinessStatus(brno);
    }
}