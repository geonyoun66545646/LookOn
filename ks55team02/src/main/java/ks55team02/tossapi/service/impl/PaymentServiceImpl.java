package ks55team02.tossapi.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import ks55team02.customer.coupons.domain.Coupons;
import ks55team02.tossapi.domain.PayOrderDTO;
import ks55team02.tossapi.domain.PaymentDTO;
import ks55team02.tossapi.domain.PaymentHistoryDTO;
import ks55team02.tossapi.mapper.PaymentMapper;
import ks55team02.tossapi.service.PaymentService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Value("${toss.secret-key}")
    private String secretKey;
    
    private final RestTemplate restTemplate;
    private final PaymentMapper paymentMapper;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public PaymentServiceImpl(PaymentMapper paymentMapper, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.paymentMapper = paymentMapper;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }   
    
    @PostConstruct
    public void init() {
        if (secretKey != null && !secretKey.isEmpty()) {
            log.info("[DEBUG] Loaded Toss Secret Key (first 5 chars): {}", secretKey.substring(0, Math.min(secretKey.length(), 5)));
            // log.info("[DEBUG] Loaded Toss Secret Key (FULL, only for debugging): {}", secretKey); // 개발 시에만 사용
        } else {
            log.warn("[DEBUG] Toss Secret Key is null or empty!");
        }
    }

    @Override
    public void saveOrder(Map<String, Object> orderData) {
        log.info("saveOrder 서비스 호출 시작. 전달받은 주문 데이터: {}", orderData);
        
        try {
            // ===========================================
            // 1. orders 테이블에 주문 정보 저장 (OrderDTO 사용)
            // ===========================================
            PayOrderDTO payorderDTO = new PayOrderDTO();
            String orderId = paymentMapper.selectNextOrderId(); 
            payorderDTO.setOrdrNo(orderId);
            
            // userNo는 세션 등에서 받아오거나 orderData에 추가되어야 합니다.
            // 현재 로그에는 userNo가 보이지 않으므로, 임시로 null 처리되거나,
            // 실제 구현에서 HttpSession에서 LoginUser를 가져와 설정해야 합니다.
            // 예: orderDTO.setUserNo((String) orderData.get("userNo"));
            // 현재 로그 상의 orderData에는 userNo 키가 없습니다. 로그인 구현에 따라 추가 필요.
            // 만약 프론트엔드에서 userNo를 보내준다면 아래와 같이 사용 가능.
            payorderDTO.setUserNo((String) orderData.get("userNo")); // <-- userNo 키가 없으면 null 됨.
                                                                  // 실제 UserNo를 가져오는 로직 (예: 세션에서)이 필요합니다.

            payorderDTO.setOrdrDt(LocalDateTime.now());
            
            // 금액 정보 매핑 (프론트엔드에서 totalAmount로 오므로 매핑 조정)
            // totalAmount를 lastStlmAmt (최종 결제 금액) 및 gdsTotAmt (상품 총액)으로 매핑
            Object totalAmountObj = orderData.get("totalAmount"); // 프론트에서 totalAmount로 넘어옴
            BigDecimal totalAmount = totalAmountObj != null ? new BigDecimal(totalAmountObj.toString()) : BigDecimal.ZERO;
            
            payorderDTO.setGdsTotAmt(totalAmount); // 상품 총액 (임시로 totalAmount와 동일하게 설정)
            payorderDTO.setDlvyFeeAmt(new BigDecimal(orderData.getOrDefault("deliveryFee", 0).toString())); // 배송비 (프론트에서 deliveryFee로 올 경우)
            
            payorderDTO.setPblcnCpnId((String) orderData.get("pblcnCpnId")); 
            payorderDTO.setUserCpnId((String) orderData.get("userCpnId"));
            payorderDTO.setApldCpnDscntAmt(new BigDecimal(orderData.getOrDefault("discountAmount", 0).toString())); // 할인 금액
            
            payorderDTO.setLastStlmAmt(totalAmount); // 최종 결제 금액 (totalAmount와 동일하게 설정)

            payorderDTO.setOrdrSttsCd("CREATED");

            Map<String, Object> shippingAddress = (Map<String, Object>) orderData.get("shippingAddress");
            if (shippingAddress != null) {
            	payorderDTO.setRcvrNm((String) shippingAddress.get("recipientName"));
                // 프론트에서 'phone'으로 넘어옴, 백엔드는 'rcvrTelno' 기대
            	payorderDTO.setRcvrTelno((String) shippingAddress.get("phone")); 
            	payorderDTO.setDlvyAddr((String) shippingAddress.get("address"));
                // 'detailAddress'는 프론트에서 넘어오지 않음. (null 유지 또는 프론트에서 추가 필요)
            	payorderDTO.setDlvyDaddr((String) shippingAddress.get("detailAddress")); 
                // 프론트에서 'postcode'로 넘어옴, 백엔드는 'zip' 기대
            	payorderDTO.setZip((String) shippingAddress.get("postcode")); 
                // 프론트에서 'deliveryRequest'로 넘어옴, 백엔드는 'dlvyMemoCn' 기대
            	payorderDTO.setDlvyMemoCn((String) shippingAddress.get("deliveryRequest"));
            }
            // 프론트에서 'customerName'으로 넘어옴, 백엔드는 'userName' 기대
            payorderDTO.setUserName((String) orderData.get("customerName")); 

            log.info("orders 테이블에 주문 정보 INSERT 시도: {}", payorderDTO);
            paymentMapper.insertOrder(payorderDTO); 
            log.info("orders 테이블에 주문 정보 삽입 완료. 주문 번호: {}", orderId);

            // ===========================================
            // 2. order_items 테이블에 주문 상품 상세 정보 저장 (Map 사용)
            // ===========================================
            List<Map<String, Object>> products = (List<Map<String, Object>>) orderData.get("products");
            if (products != null && !products.isEmpty()) {
                for (Map<String, Object> product : products) {
                    product.put("orderId", orderId);
                    product.put("ordrDtlArtclNo", paymentMapper.selectNextOrderItemId());

                    // 각 필드에 대해 null 체크 및 적절한 형변환 (예: 숫자 필드)
                    // 프론트엔드에서 넘어오는 데이터의 키 이름과 정확히 일치해야 합니다.
                    Object gdsNoObj = product.get("gdsNo"); // 프론트에서 'gdsNo'로 넘어옴
                    product.put("gdsNo", gdsNoObj != null ? gdsNoObj.toString() : null); 
                    
                    Object optionCodeObj = product.get("optionCode"); // 프론트에서 'optionCode'로 넘어올 경우
                    product.put("optNo", optionCodeObj != null ? optionCodeObj.toString() : null); 

                    Object storeIdObj = product.get("storeId"); // 프론트에서 'storeId'로 넘어올 경우
                    product.put("storeId", storeIdObj != null ? storeIdObj.toString() : null); 

                    Object qtyObj = product.get("quantity"); // 프론트에서 'quantity'로 넘어옴
                    product.put("ordrQntty", qtyObj != null ? Integer.parseInt(qtyObj.toString()) : 0); 

                    Object unitPriceObj = product.get("price"); // 프론트에서 'price'로 넘어옴
                    product.put("ordrTmUntprc", unitPriceObj != null ? new BigDecimal(unitPriceObj.toString()) : BigDecimal.ZERO); 
                    
                    product.put("ordrDtlArtclDcsnCd", product.getOrDefault("ordrDtlArtclDcsnCd", "ORDERED")); 

                    paymentMapper.insertOrderItem(product);
                    log.info("order_items 테이블에 상품 상세 정보 삽입 완료: 주문 상세 항목 번호 - {}", product.get("ordrDtlArtclNo"));
                }
            } else {
                log.warn("주문 상품 목록(products)이 없거나 비어 있습니다. order_items 테이블에 저장할 내용이 없습니다.");
            }

            // ===========================================
            // 3. payments 테이블에 결제 정보 저장 (PaymentDTO 사용, 초기 상태: READY)
            // ===========================================
            PaymentDTO paymentDTO = new PaymentDTO();
            String stlmId = paymentMapper.selectNextPaymentId();
            paymentDTO.setStlmId(stlmId);
            paymentDTO.setOrdrNo(orderId);
            paymentDTO.setUserNo(payorderDTO.getUserNo()); // orderDTO에서 userNo 가져오기
            
            // stlmMthdCd는 프론트엔드에서 어떤 방식으로 넘어오는지 확인 후 매핑 필요.
            // 현재 로그에는 'method' 키가 없으므로 null이 됩니다. (프론트에서 제공 필요)
            paymentDTO.setStlmMthdCd((String) orderData.get("method")); 
            paymentDTO.setStlmAmt(payorderDTO.getLastStlmAmt()); // 최종 결제 금액 사용
            paymentDTO.setStlmSttsCd("READY");
            
            // pgDlngId는 백엔드에서 생성한 주문번호 (orderNo)로 설정
            paymentDTO.setPgDlngId(orderId); // orderId 대신 백엔드 생성 주문번호 사용
            
            paymentDTO.setPgCoInfo("Toss Payments");
            paymentDTO.setStlmDmndDt(LocalDateTime.now());

            log.info("payments 테이블에 결제 정보 INSERT 시도: {}", paymentDTO);
            paymentMapper.insertPayment(paymentDTO); 
            log.info("payments 테이블에 결제 정보 삽입 완료. 결제 ID: {}", stlmId);

            // ===========================================
            // 4. payment_history 테이블에 결제 이력 저장 (PaymentHistoryDTO 사용, 초기 'READY' 상태 이력)
            // ===========================================
            PaymentHistoryDTO historyDTO = new PaymentHistoryDTO();
            historyDTO.setStlmHstryId(paymentMapper.selectNextPaymentHistoryId());
            historyDTO.setStlmId(stlmId);
            historyDTO.setHstryCrtDt(LocalDateTime.now());
            historyDTO.setHstryMdfcnDt(LocalDateTime.now());

            log.info("payment_history 테이블에 결제 이력 INSERT 시도: {}", historyDTO);
            paymentMapper.insertPaymentHistory(historyDTO); 
            log.info("payment_history 테이블에 결제 이력 삽입 완료. 이력 ID: {}", historyDTO.getStlmHstryId());

            orderData.put("generatedOrdrNo", orderId);
            orderData.put("generatedStlmId", stlmId);

        } catch (Exception e) {
            log.error("saveOrder 서비스 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("주문/결제 정보 저장 실패", e);
        }
    }

    @Override
    public Map<String, Object> confirmTossPayment(String paymentKey, String orderId, Long amount) throws Exception {
        log.info("confirmTossPayment 서비스 호출 시작. paymentKey: {}, orderId: {}, amount: {}", paymentKey, orderId, amount);

        String tossPaymentsUrl = "https://api.tosspayments.com/v1/payments/confirm";

        HttpHeaders headers = new HttpHeaders();
        // ★★★ 이 부분을 수정해야 합니다. ★★★
        // Secret Key에 콜론(:)을 붙여서 Base64 인코딩한 후, "Basic " 접두사를 붙여서 직접 헤더에 추가합니다.
        // `setBasicAuth` 대신 `set` 메서드를 사용해야 합니다.
        String authorizationHeader = "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", authorizationHeader);
        
        log.info("[DEBUG] Authorization Header (first 20 chars): {}", authorizationHeader.substring(0, Math.min(authorizationHeader.length(), 20))); // 보안상 전체 노출은 피함
        
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // HttpEntity 생성 (RequestBody와 Headers를 포함)
        Map<String, Object> requestBody = new HashMap<>(); // Long 타입을 위해 Object로 변경
        requestBody.put("paymentKey", paymentKey);
        requestBody.put("orderId", orderId);
        requestBody.put("amount", amount); // Long 타입 그대로 전달

        log.info("[DEBUG] Toss API Request Body: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);

        Map<String, Object> tossApiResponse;
        try {
            // HttpEntity를 사용하여 요청 본문과 헤더를 함께 보냅니다.
            tossApiResponse = restTemplate.postForObject(tossPaymentsUrl, new org.springframework.http.HttpEntity<>(requestBody, headers), Map.class);
            log.info("토스페이먼츠 승인 API 응답: {}", tossApiResponse);

            if (tossApiResponse == null || !"DONE".equals(tossApiResponse.get("status"))) {
                String code = (String) tossApiResponse.getOrDefault("code", "UNKNOWN_ERROR");
                String message = (String) tossApiResponse.getOrDefault("message", "토스페이먼츠 결제 승인 실패");
                log.error("토스페이먼츠 결제 승인 실패: 코드 = {}, 메시지 = {}", code, message);
                // 토스 API 응답에 에러 코드가 있다면 더 자세히 로깅
                if (tossApiResponse.containsKey("code") && tossApiResponse.containsKey("message")) {
                    log.error("Toss Payments Error Response: code={}, message={}", tossApiResponse.get("code"), tossApiResponse.get("message"));
                }
                throw new RuntimeException("토스페이먼츠 결제 승인 실패: " + message);
            }
        } catch (Exception e) {
            log.error("토스페이먼츠 API 호출 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("토스페이먼츠 API 호출 실패", e);
        }

        // --- 기존 DB 업데이트 로직은 그대로 유지 ---
        try {
            // ===========================================
            // 1. orders 테이블의 주문 상태를 '결제완료'로 업데이트
            // ===========================================
            Map<String, Object> orderStatusParams = new HashMap<>();
            orderStatusParams.put("orderId", orderId);
            orderStatusParams.put("status", "PAID");
            log.info("orders 테이블의 주문 상태 업데이트 시도: 주문번호 - {}, 상태 코드 - {}", orderId, "PAID");
            paymentMapper.updateOrderStatus(orderStatusParams); 
            log.info("주문 상태가 'PAID'로 업데이트되었습니다. 주문 번호: {}", orderId);

            // ===========================================
            // 2. payments 테이블의 결제 상태를 'DONE'으로 업데이트 및 완료 일시 설정
            // ===========================================
            Map<String, Object> paymentUpdateParams = new HashMap<>();
            // paymentKey가 아닌 pgDlngId를 사용해야 함.
            paymentUpdateParams.put("pgDlngId", paymentKey); // paymentKey가 토스의 pgDlngId에 해당
            paymentUpdateParams.put("orderId", orderId); // 주문번호 (필요하다면)
            paymentUpdateParams.put("stlmSttsCd", "DONE");

            log.info("payments 테이블 결제 상태 및 완료 일시 업데이트 시도: paymentKey - {}, orderId - {}, 상태 - {}", paymentKey, orderId, "DONE");
            paymentMapper.updatePaymentStatusAndCompletionDate(paymentUpdateParams); 
            log.info("payments 테이블의 결제 상태(DONE) 및 완료 일시가 업데이트되었습니다. paymentKey: {}, orderId: {}", paymentKey, orderId);

            // ===========================================
            // 3. payment_history 테이블에 '결제 완료' 이력 추가
            // ===========================================
            String newPaymentHistoryId = paymentMapper.selectNextPaymentHistoryId();
            PaymentHistoryDTO historyDTO = new PaymentHistoryDTO();
            historyDTO.setStlmHstryId(newPaymentHistoryId);
            // paymentKey를 통해 stlm_id를 조회하여 설정합니다.
            historyDTO.setStlmId(paymentMapper.getPaymentIdByPgDlngId(paymentKey)); 
            historyDTO.setHstryCrtDt(LocalDateTime.now());
            historyDTO.setHstryMdfcnDt(LocalDateTime.now());

            log.info("payment_history 테이블에 '결제 완료' 이력 추가 시도: 이력 ID - {}", newPaymentHistoryId);
            paymentMapper.insertPaymentHistory(historyDTO); 
            log.info("결제 이력 정보에 '결제 완료' 이력이 추가되었습니다. 이력 ID: {}", historyDTO.getStlmHstryId());

            // ===========================================
            // 4. (선택 사항) 사용된 쿠폰 상태를 '사용됨'으로 업데이트
            // ===========================================
            PayOrderDTO orderDetails = paymentMapper.getOrderDetailsByOrderId(orderId); 
            if (orderDetails != null && orderDetails.getUserCpnId() != null && !orderDetails.getUserCpnId().isEmpty()) {
                String userCpnId = orderDetails.getUserCpnId();
                paymentMapper.updateUserCouponToUsed(userCpnId); 
                log.info("사용자 쿠폰 ({}) 상태가 '사용됨'으로 업데이트되었습니다.", userCpnId);
            }
        } catch (Exception e) {
            log.error("토스 결제 승인 후 DB 업데이트 중 오류 발생 (paymentKey: {}, orderId: {}): {}", paymentKey, orderId, e.getMessage(), e);
            throw new RuntimeException("결제 승인 후 DB 처리 실패", e);
        }

        return tossApiResponse;
    }

    @Override
    public String createOrder(Map<String, Object> orderData) {
        log.info("createOrder 서비스 호출. orderData: {}", orderData);
        saveOrder(orderData); 
        return (String) orderData.get("generatedOrdrNo"); 
    }

    @Override
    public List<Map<String, Object>> getOrderPaymentHistoryByUser(String userNo) {
        return paymentMapper.getUserOrderPaymentHistory(userNo);
    }

    @Override
    public Map<String, Object> getLatestOrderDetailsForUser(String userNo) {
        Map<String, Object> latestOrderDetails = new HashMap<>();
        try {
            String latestOrderId = paymentMapper.findLatestOrderIdByUserNo(userNo);
            if (latestOrderId != null) {
                latestOrderDetails.put("orderInfo", paymentMapper.getOrderDetailsByOrderId(latestOrderId));
                latestOrderDetails.put("paymentInfo", paymentMapper.getPaymentDetailsByOrderId(latestOrderId));
                latestOrderDetails.put("products", paymentMapper.getOrderedProductsByOrderId(latestOrderId));
            }
        } catch (Exception e) {
            log.error("최근 주문 상세 정보 조회 중 오류 발생: {}", e.getMessage(), e);
        }
        return latestOrderDetails;
    }

    @Override
    public Long calculateDiscountedAmount(Long originalAmount, String couponCode, String userNo) {
        try {
            Map<String, Object> couponDetails = paymentMapper.getUserCouponDetails(userNo, couponCode);
            if (couponDetails != null) {
                String discountType = (String) couponDetails.get("dscntTpCd");
                BigDecimal discountValue = (BigDecimal) couponDetails.get("dscntVl");
                BigDecimal minOrderAmount = (BigDecimal) couponDetails.get("minOrdrAmt");
                BigDecimal maxDiscountAmount = (BigDecimal) couponDetails.get("maxDscntAmt");

                BigDecimal finalAmount = new BigDecimal(originalAmount);
                BigDecimal appliedDiscount = BigDecimal.ZERO;

                if (minOrderAmount != null && finalAmount.compareTo(minOrderAmount) < 0) {
                    log.warn("쿠폰 적용 실패: 최소 주문 금액 ({}) 미달. 현재 금액: {}", minOrderAmount, originalAmount);
                    return originalAmount;
                }

                if ("AMOUNT".equals(discountType)) {
                    appliedDiscount = discountValue;
                } else if ("RATE".equals(discountType)) {
                    appliedDiscount = finalAmount.multiply(discountValue.divide(new BigDecimal(100)));
                }

                if (maxDiscountAmount != null && appliedDiscount.compareTo(maxDiscountAmount) > 0) {
                    appliedDiscount = maxDiscountAmount;
                }
                
                finalAmount = finalAmount.subtract(appliedDiscount);
                
                if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                    finalAmount = BigDecimal.ZERO;
                }

                log.info("쿠폰 적용 성공. 원본 금액: {}, 할인 금액: {}, 최종 금액: {}", originalAmount, appliedDiscount, finalAmount);
                return finalAmount.longValue();
            }
        } catch (Exception e) {
            log.error("쿠폰 할인 금액 계산 중 오류 발생: {}", e.getMessage(), e);
        }
        return originalAmount;
    }

    @Override
    public List<Map<String, Object>> getUserCoupons(String userNo) {
        return paymentMapper.selectUserCoupons(userNo);
    }
    
    @Override
    public Map<String, Object> fakeConfirmTossPayment(String paymentKey, String orderId, Long amount) {
        log.warn("<<<<< fakeConfirmTossPayment: 실제 API 호출 대신 가짜 데이터를 생성합니다. >>>>>");
        Map<String, Object> fakeResult = new HashMap<>();
        fakeResult.put("status", "DONE");
        fakeResult.put("paymentKey", paymentKey);
        fakeResult.put("orderId", orderId);
        fakeResult.put("amount", amount);
        fakeResult.put("method", "카드");
        fakeResult.put("approvedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return fakeResult;
    }
    
    /**
     * <<<<<<<<< [추가] 주문 ID로 주문 상품 목록 조회 구현 >>>>>>>>>
     */
    @Override
    public List<Map<String, Object>> getOrderedProductsByOrderId(String orderId) {
        log.info("주문 상품 목록 조회 서비스 호출. 주문 번호: {}", orderId);
        return paymentMapper.getOrderedProductsByOrderId(orderId);
    }
    
    @Override
    public List<Coupons> getUserCouponsDTO(String userNo) {
        return paymentMapper.findUserCouponsByUserId(userNo); // Mapper 메서드 호출
    }

}