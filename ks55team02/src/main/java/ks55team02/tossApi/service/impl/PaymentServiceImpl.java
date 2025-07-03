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
        orderDTO.setUserNo("temp-user-01"); 
        
        orderDTO.setOrdrDt(LocalDateTime.now());
        
        // --- 변경 시작 ---
        // 프론트에서 보낸 'gdsTotAmt' 키를 사용합니다.
        // null 체크를 추가하여 안전하게 변환합니다.
        Object gdsTotAmtObj = orderData.get("gdsTotAmt");
        if (gdsTotAmtObj != null) {
            orderDTO.setGdsTotAmt(new BigDecimal(gdsTotAmtObj.toString()));
        } else {
            log.warn("orderData에 gdsTotAmt 키가 없거나 null입니다.");
            // 기본값을 설정하거나, 예외를 던지거나, 비즈니스 로직에 맞게 처리
            orderDTO.setGdsTotAmt(BigDecimal.ZERO); // 예: 0으로 설정
        }
        
        // 'lastStlmAmt'는 최종 결제 금액으로 토스페이먼츠 승인 요청에 사용되지만, OrderDTO에도 저장한다면 추가
        // Object lastStlmAmtObj = orderData.get("lastStlmAmt");
        // if (lastStlmAmtObj != null) {
        //     orderDTO.setLastStlmAmt(new BigDecimal(lastStlmAmtObj.toString()));
        // }
        // --- 변경 끝 ---

        orderDTO.setOrdrSttsCd("PENDING_PAYMENT"); // 주문 상태: 결제 대기

        // 배송지 정보 추출 및 설정
        // 이 부분은 이미 frontend와 매핑되도록 수정된 것으로 가정합니다.
        orderDTO.setRcvrNm((String) orderData.get("rcvrNm"));
        orderDTO.setRcvrTelno((String) orderData.get("rcvrTelno"));
        orderDTO.setDlvyAddr((String) orderData.get("dlvyAddr"));
        orderDTO.setDlvyDaddr((String) orderData.get("dlvyDaddr"));
        orderDTO.setZip((String) orderData.get("zip"));
        orderDTO.setDlvyMemoCn((String) orderData.get("dlvyMemoCn"));
        orderDTO.setUserName((String) orderData.get("userName")); // DTO에 userName 필드가 있다면

        log.info("OrderDTO after mapping:");
        log.info("  rcvrNm: {}", orderDTO.getRcvrNm());
        log.info("  rcvrTelno: {}", orderDTO.getRcvrTelno());
        log.info("  dlvyAddr: {}", orderDTO.getDlvyAddr());
        log.info("  dlvyDaddr: {}", orderDTO.getDlvyDaddr());
        log.info("  zip: {}", orderDTO.getZip());
        log.info("  dlvyMemoCn: {}", orderDTO.getDlvyMemoCn());
        log.info("  userName: {}", orderDTO.getUserName());
        log.info("  gdsTotAmt: {}", orderDTO.getGdsTotAmt());
        
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
            savePaymentDetails(responseBody); // responseBody는 토스에서 받은 데이터 (totalAmount 키 그대로 사용)
            
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
                log.error("Thymeleaf 표시를 위한 날짜 파싱 중 오류 발생 (무시함): {}", e.getMessage());
            }

            return responseBody;
        } else {
            log.error("결제 승인 실패: {}", responseEntity);
            // 에러 응답 바디가 있다면 메시지를 포함합니다.
            String errorMessage = "결제 승인에 실패했습니다.";
            if (responseBody != null && responseBody.containsKey("message")) {
                errorMessage += " 응답 메시지: " + responseBody.get("message");
            } else {
                errorMessage += " 응답 코드: " + responseEntity.getStatusCode();
            }
            throw new Exception(errorMessage);
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
        requestBody.put("amount", amount); // 이 amount는 토스페이먼츠로 보낼 '최종 결제 금액'

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
            
            // --- 변경 시작 ---
            // 토스페이먼츠 응답에는 'totalAmount' 키가 있습니다.
            // null 체크를 추가하여 안전하게 변환합니다.
            Object totalAmountObj = paymentData.get("totalAmount");
            if (totalAmountObj != null) {
                paymentDTO.setStlmAmt(new BigDecimal(totalAmountObj.toString()));
            } else {
                log.warn("paymentData에 totalAmount 키가 없거나 null입니다.");
                paymentDTO.setStlmAmt(BigDecimal.ZERO); // 예: 0으로 설정
            }
            // --- 변경 끝 ---

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
            
            paymentMapper.insertPaymentHistory(paymentHistoryDTO);
            log.info("결제 이력(PaymentHistory) 저장 성공! ID: {}", paymentHistoryDTO.getStlmHstryId());

            // === 3. Orders 테이블 상태 업데이트 ===
            String orderId = (String) paymentData.get("orderId");
            Map<String, Object> statusParams = new HashMap<>();
            statusParams.put("orderId", orderId);
            statusParams.put("status", "PAYMENT_COMPLETED"); // 주문 상태: '결제 완료'
            
            paymentMapper.updateOrderStatus(statusParams);
            log.info("주문 상태 '결제 완료'로 업데이트 성공. OrderId: {}", orderId);
            
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