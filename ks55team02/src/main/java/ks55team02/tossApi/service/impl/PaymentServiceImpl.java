package ks55team02.tossApi.service.impl;

import ks55team02.tossApi.domain.OrderDTO;
import ks55team02.tossApi.domain.PaymentDTO;
import ks55team02.tossApi.domain.PaymentHistoryDTO;
import ks55team02.tossApi.mapper.PaymentMapper;
import ks55team02.tossApi.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Autowired
    private PaymentMapper paymentMapper;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String TOSS_API_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    /**
     * 주문 정보를 '결제 대기' 상태로 DB에 먼저 저장합니다.
     * 외래 키 제약 조건을 만족시키기 위해 가장 먼저 실행되어야 합니다.
     */
    @Override
    @Transactional
    public String createOrder(Map<String, Object> orderData) {
    	String orderId = paymentMapper.selectNextOrderId();
        log.info("새로운 주문 번호 생성: {}", orderId);
        
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrdrNo(orderId);
        
        // TODO: 실제 로그인된 사용자 ID를 가져와야 합니다.
        // users 테이블에 이 userNo가 반드시 존재해야 합니다.
        orderDTO.setUserNo("temp-user-01"); 
        
        orderDTO.setOrdrDt(LocalDateTime.now());
        orderDTO.setGdsTotAmt(new BigDecimal(orderData.get("totalAmount").toString()));
        orderDTO.setOrdrSttsCd("PENDING_PAYMENT"); // 주문 상태: 결제 대기

        // 배송지 정보 추출 및 설정
        if (orderData.get("shippingAddress") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> shippingAddress = (Map<String, String>) orderData.get("shippingAddress");
            orderDTO.setRcvrNm((String) orderData.get("customerName"));
            // orderDTO.setRcvrTelno(...); // 전화번호가 있다면 설정
            orderDTO.setDlvyAddr(shippingAddress.get("address"));
            orderDTO.setDlvyDaddr(shippingAddress.get("detailAddress"));
            orderDTO.setZip(shippingAddress.get("postcode"));
            orderDTO.setDlvyMemoCn(shippingAddress.get("deliveryRequest"));
        }
        
        paymentMapper.insertOrder(orderDTO);
        log.info("DB에 '결제 대기' 주문 정보 저장 완료. OrderId: {}", orderId);
        
        return orderId;
    }

    /**
     * 토스페이먼츠에 결제 승인을 요청하고, 성공 시 DB에 결제 정보를 저장 및 주문 상태를 업데이트합니다.
     */
    @Override
    @Transactional
    public Map<String, Object> confirmTossPayment(String paymentKey, String orderId, Long amount) throws Exception {
        log.info("결제 승인 요청 시작. PaymentKey: {}, OrderId: {}, Amount: {}", paymentKey, orderId, amount);

        // 토스페이먼츠 API 요청
        ResponseEntity<Map<String, Object>> responseEntity = requestTossPaymentConfirmation(paymentKey, orderId, amount);
        Map<String, Object> responseBody = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful() && responseBody != null) {
            log.info("결제 승인 성공: {}", responseBody);
            
            // [수정 1] DB 저장을 먼저 확실하게 끝냅니다.
            savePaymentDetails(responseBody);
            
            // [수정 2] Thymeleaf에 데이터를 전달하기 위한 후처리 작업은 별도의 try-catch로 감싸서,
            // 이 부분에서 에러가 나더라도 전체 로직이 실패하지 않도록 합니다.
            try {
                if (responseBody.get("approvedAt") instanceof String) {
                    String approvedAtStr = (String) responseBody.get("approvedAt");
                    if (approvedAtStr != null) {
                        OffsetDateTime approvedAt = OffsetDateTime.parse(approvedAtStr);
                        responseBody.put("approvedAt", approvedAt);
                    }
                }
            } catch (Exception e) {
                // 날짜 파싱에 실패하더라도 이미 DB 저장은 성공했으므로, 에러를 로깅만 하고 무시합니다.
                // 화면에는 날짜가 표시되지 않겠지만, 결제 실패 페이지로 넘어가지는 않습니다.
                log.error("Thymeleaf 표시를 위한 날짜 파싱 중 오류 발생 (무시함): {}", e.getMessage());
            }

            return responseBody;
        } else {
            log.error("결제 승인 실패: {}", responseEntity);
            throw new Exception("결제 승인에 실패했습니다. 응답 코드: " + responseEntity.getStatusCode());
        }
    }
    
    /**
     * 토스페이먼츠 결제 승인 API를 실제로 호출하는 private 메서드
     */
    private ResponseEntity<Map<String, Object>> requestTossPaymentConfirmation(String paymentKey, String orderId, Long amount) {
        HttpHeaders headers = new HttpHeaders();
        String encodedSecretKey = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.setBasicAuth(encodedSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentKey", paymentKey);
        requestBody.put("orderId", orderId);
        requestBody.put("amount", amount);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        return restTemplate.exchange(
            TOSS_API_CONFIRM_URL,
            HttpMethod.POST,
            requestEntity,
            new ParameterizedTypeReference<>() {}
        );
    }
    
    /**
     * 결제 성공 정보를 DB에 저장하고, 주문 상태를 업데이트하는 private 메서드
     */
    private void savePaymentDetails(Map<String, Object> paymentData) {
        log.info("--- savePaymentDetails 메서드 시작 ---");

        try {
            // === 1. Payment 정보 저장 ===
            String nextPaymentId = paymentMapper.selectNextPaymentId();
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setStlmId(nextPaymentId);
            paymentDTO.setOrdrNo((String) paymentData.get("orderId"));
            paymentDTO.setPgDlngId((String) paymentData.get("paymentKey"));
            paymentDTO.setStlmAmt(new BigDecimal(paymentData.get("totalAmount").toString()));
            paymentDTO.setStlmSttsCd((String) paymentData.get("status"));
            paymentDTO.setStlmMthdCd(getPaymentMethod(paymentData));
            paymentDTO.setPgCoInfo("Toss Payments");
            paymentDTO.setUserNo("temp-user-01"); // TODO: 실제 로그인된 사용자 ID로 변경 필요
            LocalDateTime now = LocalDateTime.now();
            paymentDTO.setStlmCmptnDt(now);
            paymentDTO.setStlmDmndDt(now);
            
            paymentMapper.insertPayment(paymentDTO);
            log.info("결제 정보(Payment) 저장 성공! ID: {}", paymentDTO.getStlmId());

            // === 2. PaymentHistory 정보 저장 ===
            String nextHistoryId = paymentMapper.selectNextPaymentHistoryId();
            PaymentHistoryDTO paymentHistoryDTO = new PaymentHistoryDTO();
            paymentHistoryDTO.setStlmHstryId(nextHistoryId);
            paymentHistoryDTO.setStlmId(paymentDTO.getStlmId());
            paymentHistoryDTO.setHstryCrtDt(now);
            paymentHistoryDTO.setHstryMdfcnDt(now);
            
            paymentMapper.insertPaymentHistory(paymentHistoryDTO);
            log.info("결제 이력(PaymentHistory) 저장 성공! ID: {}", paymentHistoryDTO.getStlmHstryId());

            // --- ▼▼▼▼▼▼ [중요] 이 부분이 추가/수정되었습니다 ▼▼▼▼▼▼ ---
            // === 3. Orders 테이블 상태 업데이트 ===
            String orderId = (String) paymentData.get("orderId");
            Map<String, Object> statusParams = new HashMap<>();
            statusParams.put("orderId", orderId);
            statusParams.put("status", "PAYMENT_COMPLETED"); // 주문 상태: '결제 완료'
            
            paymentMapper.updateOrderStatus(statusParams);
            log.info("주문 상태 '결제 완료'로 업데이트 성공. OrderId: {}", orderId);
            // --- ▲▲▲▲▲▲ 여기까지 추가/수정 ▲▲▲▲▲▲ ---
            
        } catch (Exception e) {
            log.error("!!!!!!!!!! DB 저장 중 심각한 오류 발생 !!!!!!!!!!", e);
            throw new RuntimeException("DB 저장 실패", e);
        }

        log.info("--- savePaymentDetails 메서드 정상 종료 ---");
    }

    /**
     * 토스 API 응답에서 결제 수단 문자열을 추출하는 헬퍼 메서드
     */
    private String getPaymentMethod(Map<String, Object> paymentData) {
        return (String) paymentData.get("method");
    }
}