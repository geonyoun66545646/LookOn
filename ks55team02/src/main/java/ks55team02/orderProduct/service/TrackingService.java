package ks55team02.orderProduct.service;

import com.fasterxml.jackson.databind.ObjectMapper; // Jackson ObjectMapper 임포트
import ks55team02.orderProduct.domain.SweetTrackerDataDTO;		// Mocking을 위해 추가
import ks55team02.orderProduct.domain.SweetTrackerResponseDTO;	// 새로 만든 DTO 임포트
import ks55team02.orderProduct.domain.TrackingDetailDTO;		// Mocking을 위해 추가

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays; // Mocking을 위해 추가
import java.util.Collections; // Mocking을 위해 추가
import java.util.List; // Mocking을 위해 추가

@Service
public class TrackingService {

    @Value("${sweettracker.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper; // ObjectMapper 주입

    public TrackingService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 스마트택배 API를 통해 운송장 정보를 조회하고, JSON 응답을 SweetTrackerResponse 객체로 파싱합니다.
     * @param invoiceNo 운송장 번호
     * @param courierCode 택배사 코드 (예: CJ대한통운은 "KR.CJLogistics")
     * @return 파싱된 SweetTrackerResponse 객체 (오류 시 null 또는 예외 처리)
     */
    public SweetTrackerResponseDTO getTrackingInfo(String invoiceNo, String courierCode) {
        // --- 테스트를 위한 Mock 데이터 반환 시작 (테스트 후 반드시 주석 처리하거나 삭제하세요!) ---
        // 실제 배송 중인 운송장 번호가 없을 때 UI 및 로직 테스트용으로 사용합니다.
        if ("TEST12345".equals(invoiceNo)) { // 특정 운송장 번호로 Mock 응답 반환
        	SweetTrackerResponseDTO mockResponse = new SweetTrackerResponseDTO();
            mockResponse.setCode("200");
            mockResponse.setMessage("success");

            SweetTrackerDataDTO mockData = new SweetTrackerDataDTO();
            mockData.setInvoiceNo(invoiceNo);
            mockData.setSenderName("테스트발송자");
            mockData.setReceiverName("테스트수령자");
            mockData.setItemName("테스트상품");
            mockData.setItemQty("1");
            // 여기에 원하는 배송 상태를 설정해보세요! (예: "상품인수", "상품이동중", "배달지도착", "배달출발", "배달완료")
            mockData.setStatus("배달완료"); // 현재 테스트할 배송 상태

            // 배송 상세 이력 (원하는 상태에 맞춰 이력을 추가/수정)
            List<TrackingDetailDTO> trackingDetails = Arrays.asList(
                createDetail("2025-07-01 10:00:00", "서울하나로", "상품인수"),
                createDetail("2025-07-01 15:00:00", "옥천HUB", "상품이동중"),
                createDetail("2025-07-02 09:00:00", "부산터미널", "배달지도착"),
                createDetail("2025-07-02 11:30:00", "부산사상", "배달출발"),
                createDetail("2025-07-02 14:00:00", "부산사상", "배달완료", "010-1234-5678", "김기사")
            );
            mockData.setTrackingDetails(trackingDetails);
            mockResponse.setData(mockData);

            System.out.println("--- Mock 데이터 반환 (운송장: " + invoiceNo + ", 상태: " + mockData.getStatus() + ") ---");
            return mockResponse;
        }
        // --- 테스트를 위한 Mock 데이터 반환 끝 ---


        // 아래는 실제 API 호출 로직입니다. Mock 테스트가 끝나면 이 부분을 사용해야 합니다.
        String apiUrl = String.format("https://apis.tracker.delivery/carriers/%s/tracks/%s?t_key=%s",
                                      courierCode, invoiceNo, apiKey);

        try {
            String jsonResponse = restTemplate.getForObject(apiUrl, String.class);
            System.out.println("SweetTracker API 응답 JSON: " + jsonResponse); // 콘솔에 응답 출력

            // ObjectMapper를 사용하여 JSON 문자열을 SweetTrackerResponse 객체로 파싱
            return objectMapper.readValue(jsonResponse, SweetTrackerResponseDTO.class);

        } catch (Exception e) {
            System.err.println("API 호출 또는 JSON 파싱 중 오류 발생: " + e.getMessage());
            // 실제 애플리케이션에서는 로깅 프레임워크 사용
            return null; // 오류 발생 시 null 반환
        }
    }

    // TrackingDetail 객체를 쉽게 생성하기 위한 헬퍼 메소드
    private TrackingDetailDTO createDetail(String time, String location, String description) {
    	TrackingDetailDTO detail = new TrackingDetailDTO();
        detail.setTime(time);
        detail.setLocation(location);
        detail.setDescription(description);
        return detail;
    }

    private TrackingDetailDTO createDetail(String time, String location, String description, String telno, String manName) {
    	TrackingDetailDTO detail = createDetail(time, location, description);
        detail.setTelno(telno);
        detail.setManName(manName);
        return detail;
    }
}
