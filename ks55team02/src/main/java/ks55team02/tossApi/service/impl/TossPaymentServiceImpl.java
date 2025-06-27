package ks55team02.tossApi.service.impl;

//Java 표준 라이브러리 import
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//Spring Framework 관련 클래스 import
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import ks55team02.tossApi.domain.PaymentHistory;
//직접 만든 클래스 import
import ks55team02.tossApi.domain.Payment;
import ks55team02.tossApi.domain.TossConfirmRequestDto;
import ks55team02.tossApi.domain.TossConfirmResponseDto;
import ks55team02.tossApi.mapper.PaymentMapper;
import ks55team02.tossApi.service.TossPaymentService;
//Lombok 어노테이션 import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로그 출력을 위한 어노테이션


@Service
@RequiredArgsConstructor
@Slf4j // 로그 출력을 위한 Lombok 어노테이션 (Slf4j : Simple Logging Facade for Java)
public class TossPaymentServiceImpl implements TossPaymentService {

 private final RestTemplate restTemplate; // Bean으로 등록한 RestTemplate 주입
 private final PaymentMapper paymentMapper;

 @Value("${toss.secret-key}")
 private String tossSecretKey;

 @Value("${toss.api-url}")
 private String tossApiUrl;

 @Override
 @Transactional
 public Map<String, Object> confirmPayment(String paymentKey, String orderId, Long amount) {
     // 1. 토스페이먼츠 API에 최종 결제 승인 요청
     TossConfirmResponseDto tossResponse = requestTossPaymentConfirmation(paymentKey, orderId, amount);
     log.info("토스 API 응답 성공: {}", tossResponse);

     // 2. 응답 데이터를 기반으로 우리 시스템의 Payment 객체 생성
     String currentUserId = "user-01"; // TODO: 실제 로그인한 사용자 ID로 교체
     Payment payment = createPaymentFromResponse(tossResponse, currentUserId);
     
     // 3. 생성된 Payment 객체를 DB에 저장
     paymentMapper.savePayment(payment);
     log.info("DB에 결제 정보 저장 성공: {}", payment);

     // 4. 결제 이력(PaymentHistory) 객체 생성 및 DB 저장
     PaymentHistory history = createPaymentHistory(payment.getStlmId());
     paymentMapper.savePaymentHistory(history);
     log.info("DB에 결제 이력 저장 성공: {}", history);

     // 5. 컨트롤러에 반환할 결과 구성
     Map<String, Object> result = new HashMap<>();
     result.put("status", "success");
     result.put("message", "결제가 성공적으로 완료 및 저장되었습니다.");
     result.put("payment", payment);

     return result;
 }

 private TossConfirmResponseDto requestTossPaymentConfirmation(String paymentKey, String orderId, Long amount) {
     final String url = tossApiUrl + "/confirm";

     TossConfirmRequestDto requestDto = new TossConfirmRequestDto(paymentKey, orderId, amount);

     HttpHeaders headers = new HttpHeaders();
     headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8)));
     headers.setContentType(MediaType.APPLICATION_JSON);

     HttpEntity<TossConfirmRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);

     try {
         return restTemplate.postForObject(url, requestEntity, TossConfirmResponseDto.class);
     } catch (HttpClientErrorException e) {
         log.error("토스 API 연동 실패. status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
         throw new RuntimeException("토스페이먼츠 API 연동에 실패했습니다. 응답: " + e.getResponseBodyAsString(), e);
     } catch (Exception e) {
         log.error("토스 API 연동 중 알 수 없는 오류 발생.", e);
         throw new RuntimeException("토스페이먼츠 API 연동 중 오류가 발생했습니다.", e);
     }
 }
 
 private Payment createPaymentFromResponse(TossConfirmResponseDto tossResponse, String userId) {
     Payment payment = new Payment();
     payment.setStlmId(tossResponse.getPaymentKey());
     payment.setOrdrNo(tossResponse.getOrderId());
     payment.setUserNo(userId);
     payment.setStlmMthdCd(tossResponse.getMethod());
     payment.setStlmAmt(tossResponse.getTotalAmount());
     payment.setStlmSttsCd(tossResponse.getStatus());
     payment.setPgDlngId(tossResponse.getPaymentKey());

     // tossResponse.getCard()가 null이 아닌지 확인하여 NullPointerException 방지
     if (tossResponse.getCard() != null) {
         payment.setPgCoInfo(tossResponse.getCard().getCompany());
     } else {
         payment.setPgCoInfo("토스페이먼츠-" + tossResponse.getMethod());
     }

     DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
     payment.setStlmCmptnDt(LocalDateTime.parse(tossResponse.getApprovedAt(), formatter));
     payment.setStlmDmndDt(LocalDateTime.parse(tossResponse.getRequestedAt(), formatter));
     return payment;
 }

 private PaymentHistory createPaymentHistory(String paymentId) {
     PaymentHistory history = new PaymentHistory();
     history.setStlmHstryId("hist_" + UUID.randomUUID().toString().replace("-", ""));
     history.setStlmId(paymentId);
     history.setHstryCrtDt(LocalDateTime.now());
     history.setHstryMdfcnDt(LocalDateTime.now());
     return history;
 }
}