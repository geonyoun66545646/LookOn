package ks55team02.tossapi.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import ks55team02.customer.coupons.domain.UserCoupons;
import ks55team02.tossapi.domain.PayOrderDTO;
import ks55team02.tossapi.domain.PaymentDTO;
import ks55team02.tossapi.domain.PaymentHistoryDTO;
import ks55team02.tossapi.mapper.PaymentMapper;
import ks55team02.tossapi.service.PaymentService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;

// ★★★ 추가/확인 필요 ★★★
import java.util.LinkedHashMap; // LinkedHashMap 임포트 확인 (Map 순서 유지를 위해 사용될 수 있음)
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
    
    @Autowired
    public PaymentServiceImpl(PaymentMapper paymentMapper, RestTemplate restTemplate) {
        this.paymentMapper = paymentMapper;
        this.restTemplate = restTemplate;
    }   
    
    @Override
    public String createOrder(Map<String, Object> orderData) {
        log.info("createOrder 서비스 호출. orderData: {}", orderData);
        return saveOrderAndReturnId(orderData); 
    }

    private String saveOrderAndReturnId(Map<String, Object> orderData) {
        log.info("saveOrderAndReturnId 내부 로직 시작. 전달받은 주문 데이터: {}", orderData);
        
        try {
            // 1. 주문 정보 저장 (orders 테이블)
            PayOrderDTO payorderDTO = new PayOrderDTO();
            String orderId = paymentMapper.selectNextOrderId(); 
            payorderDTO.setOrdrNo(orderId);
            payorderDTO.setUserNo((String) orderData.get("userNo"));
            payorderDTO.setOrdrDt(LocalDateTime.now());
            payorderDTO.setGdsTotAmt(getBigDecimalFromMap(orderData, "totalAmount"));
            payorderDTO.setApldCpnDscntAmt(getBigDecimalFromMap(orderData, "discountAmount"));
            payorderDTO.setLastStlmAmt(getBigDecimalFromMap(orderData, "finalAmount"));
            payorderDTO.setDlvyFeeAmt(BigDecimal.ZERO);
            payorderDTO.setUserCpnId((String) orderData.get("couponId")); 
            payorderDTO.setOrdrSttsCd("CREATED");

            Map<String, Object> shippingAddress = (Map<String, Object>) orderData.get("shippingAddress");
            if (shippingAddress != null) {
            	payorderDTO.setRcvrNm((String) shippingAddress.get("recipientName"));
            	payorderDTO.setRcvrTelno((String) shippingAddress.get("phone")); 
            	payorderDTO.setDlvyAddr((String) shippingAddress.get("address"));
            	payorderDTO.setDlvyDaddr((String) shippingAddress.get("detailAddress"));
            	payorderDTO.setZip((String) shippingAddress.get("postcode")); 
            	payorderDTO.setDlvyMemoCn((String) shippingAddress.get("deliveryRequest"));
            }
            payorderDTO.setUserName((String) orderData.get("customerName")); 

            paymentMapper.insertOrder(payorderDTO); 
            log.info("orders 테이블 저장 완료. 주문 번호: {}", orderId);

            // 2. 주문 상품 정보 저장 (order_items 테이블)
            List<Map<String, Object>> products = (List<Map<String, Object>>) orderData.get("products");
            if (products != null && !products.isEmpty()) {
                for (Map<String, Object> product : products) {
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("ordrDtlArtclNo", paymentMapper.selectNextOrderItemId());
                    itemData.put("ordrNo", orderId);
                    itemData.put("gdsNo", product.get("gdsNo"));
                    itemData.put("ordrQntty", product.get("quantity"));
                    itemData.put("ordrTmUntprc", product.get("price"));
                    itemData.put("ordrDtlArtclDcsnCd", "ORDERED");
                    
                    // ★★★ 수정된 부분 ★★★: 프론트에서 보내는 'store_id' 키로 값을 가져옴
                    itemData.put("storeId", product.get("store_id"));
                    
                    paymentMapper.insertOrderItem(itemData);
                }
            }

            // 3. 결제 정보 저장 (payments 테이블)
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setStlmId(paymentMapper.selectNextPaymentId());
            paymentDTO.setOrdrNo(orderId);
            paymentDTO.setUserNo(payorderDTO.getUserNo());
            paymentDTO.setStlmAmt(payorderDTO.getLastStlmAmt());
            paymentDTO.setStlmSttsCd("READY");
            paymentDTO.setPgDlngId(orderId);
            paymentDTO.setPgCoInfo("Toss Payments");
            paymentDTO.setStlmDmndDt(LocalDateTime.now());
            paymentMapper.insertPayment(paymentDTO); 

            return orderId;

        } catch (Exception e) {
            log.error("saveOrder 서비스 처리 중 심각한 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("주문 정보 저장 실패. 관리자에게 문의하세요.", e);
        }
    }

    private BigDecimal getBigDecimalFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return BigDecimal.ZERO;
        }
        try {
            if (value instanceof BigDecimal) {
                return (BigDecimal) value;
            } else if (value instanceof Number) {
                return new BigDecimal(value.toString());
            } else if (value instanceof String) {
                return new BigDecimal((String) value);
            }
            return BigDecimal.ZERO;
        } catch (NumberFormatException e) {
            log.warn("BigDecimal 변환 실패. key: {}, value: {}. 오류: {}", key, value, e.getMessage());
            return BigDecimal.ZERO;
        }
    }


    @Override
    public Map<String, Object> confirmTossPayment(String paymentKey, String orderId, Long amount) throws Exception {
        
        String tossPaymentsUrl = "https://api.tosspayments.com/v1/payments/confirm";
        
        HttpHeaders headers = new HttpHeaders();
        String authorizationHeader = "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", authorizationHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, Object> requestBody = Map.of("paymentKey", paymentKey, "orderId", orderId, "amount", amount);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        Map<String, Object> tossApiResponse;
        try {
            tossApiResponse = restTemplate.postForObject(tossPaymentsUrl, requestEntity, Map.class);
            log.info("토스페이먼츠 승인 API 응답: {}", tossApiResponse);

            if (tossApiResponse == null || !"DONE".equals(tossApiResponse.get("status"))) {
                String errorCode = (String) tossApiResponse.get("code");
                String errorMessage = (String) tossApiResponse.get("message");
                log.error("토스페이먼츠 결제 승인 실패. 코드: {}, 메시지: {}", errorCode, errorMessage);
                throw new RuntimeException("토스페이먼츠 결제 승인 실패: " + errorMessage);
            }
        } catch (Exception e) {
            log.error("토스페이먼츠 API 호출 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("토스페이먼츠 API 호출 실패: " + e.getMessage(), e);
        }

        try {
            List<Map<String, Object>> orderedItems = paymentMapper.getOrderedProductsByOrderId(orderId);
            log.info(">>>> [DEBUG] Ordered Items from DB: {}", orderedItems);

            Map<String, BigDecimal> salesByStore = new HashMap<>();
            for (Map<String, Object> item : orderedItems) {
                String storeId = (String) item.get("storeId");
                Object unitPriceObj = item.get("price");
                Object quantityObj = item.get("quantity");
                
                log.info(">>>> [DEBUG] Processing Item - storeId: {}, unitPrice: {}, quantity: {}", storeId, unitPriceObj, quantityObj);

                BigDecimal unitPrice = BigDecimal.ZERO;
                if (unitPriceObj instanceof BigDecimal) {
                    unitPrice = (BigDecimal) unitPriceObj;
                } else if (unitPriceObj instanceof Number) {
                    unitPrice = new BigDecimal(unitPriceObj.toString());
                }

                int quantity = 0;
                if (quantityObj instanceof Integer) {
                    quantity = (Integer) quantityObj;
                } else if (quantityObj instanceof Number) {
                    quantity = ((Number) quantityObj).intValue();
                }

                if (storeId != null && !storeId.isEmpty() && unitPrice.compareTo(BigDecimal.ZERO) > 0 && quantity > 0) {
                    log.info(">>>> [DEBUG] Condition PASSED for storeId: {}", storeId);
                    BigDecimal salesAmount = unitPrice.multiply(new BigDecimal(quantity));
                    salesByStore.put(storeId, salesByStore.getOrDefault(storeId, BigDecimal.ZERO).add(salesAmount));
                } else {
                    log.warn(">>>> [DEBUG] Condition FAILED for item. Check storeId, price type, or quantity type. Item: {}", item);
                }
            }

            log.info(">>>> [DEBUG] Final Sales By Store Map: {}", salesByStore);
            
            if (!salesByStore.isEmpty()) {
                log.info("상점별 매출 누적 업데이트는 현재 비활성화 상태입니다.");
                // 로직이 필요하다면 여기에 추가 (예: paymentMapper.updateTotalSalesAmount 호출)
            } else {
                log.warn(">>>> [DEBUG] salesByStore Map is empty. No updates will be made.");
            }

            // ★★★ 수정된 부분 ★★★: getOrderDetailsByOrderId -> getOrderByOrdrNo 로 변경
            PayOrderDTO orderDetails = paymentMapper.getOrderByOrdrNo(orderId);
            
            Map<String, Object> orderStatusParams = Map.of("ordrNo", orderId, "ordrStatus", "PAID");
            paymentMapper.updateOrderStatus(orderStatusParams); 

            Map<String, Object> paymentStatusUpdateParams = new HashMap<>();
            paymentStatusUpdateParams.put("ordrNo", orderId);
            paymentStatusUpdateParams.put("stlmSttsCd", "DONE");
            paymentStatusUpdateParams.put("pgDlngId", paymentKey);
            paymentMapper.updatePaymentStatusAndCompletionDate(paymentStatusUpdateParams);

            String paymentId = paymentMapper.getPaymentIdByPgDlngId(paymentKey);
            if(paymentId != null) {
                PaymentHistoryDTO historyDTO = new PaymentHistoryDTO();
                historyDTO.setStlmHstryId(paymentMapper.selectNextPaymentHistoryId());
                historyDTO.setStlmId(paymentId);
                historyDTO.setHstryCrtDt(LocalDateTime.now());
                paymentMapper.insertPaymentHistory(historyDTO);
            }

            if (orderDetails != null && orderDetails.getUserCpnId() != null && !orderDetails.getUserCpnId().isEmpty()) {
                log.info("사용자 쿠폰 ({}) 상태 업데이트 시도.", orderDetails.getUserCpnId());
                paymentMapper.updateUserCouponToUsed(orderDetails.getUserCpnId());
                log.info("사용자 쿠폰 ({}) 상태가 '사용됨'으로 업데이트되었습니다.", orderDetails.getUserCpnId());
            } else {
                log.info("적용된 쿠폰이 없거나 userCpnId가 유효하지 않아 쿠폰 상태 업데이트를 건너뜜.");
            }
            
            String userNo = (orderDetails != null) ? orderDetails.getUserNo() : null;
            if (userNo != null && !orderedItems.isEmpty()) {
                // 장바구니 삭제 로직 (필요 시 주석 해제)
                // log.info("결제 완료된 상품들을 장바구니에서 삭제합니다. 사용자: {}", userNo);
                // paymentMapper.deletePurchasedItemsFromCart(...);
            } else {
                log.warn("장바구니 삭제를 위한 userNo 또는 orderedItems가 유효하지 않습니다.");
            }

        } catch (Exception e) {
            log.error("DB 업데이트 중 오류 발생. paymentKey: {}, orderId: {}", paymentKey, orderId, e);
            throw new RuntimeException("결제 승인 후 DB 처리 실패", e);
        }

        return tossApiResponse;
    }

    @Override
    public List<Map<String, Object>> getOrderedProductsByOrderId(String orderId) {
        return paymentMapper.getOrderedProductsByOrderId(orderId);
    }
    
    @Override
    public List<UserCoupons> getActiveUserCoupons(String userNo) {
        return paymentMapper.findUserCouponsByUserId(userNo);
    }

    @Override
    public List<Map<String, Object>> getOrderPaymentHistoryByUser(String userNo) {
        // 이 메소드가 필요하다면 PaymentMapper.xml에 getUserOrderPaymentHistory 쿼리를 정의해야 합니다.
        return new ArrayList<>(); // 현재는 비어있는 리스트 반환
    }

    @Override
    public Map<String, Object> getLatestOrderDetailsForUser(String userNo) {
        Map<String, Object> latestOrderDetails = new LinkedHashMap<>();
        try {
            String latestOrderId = paymentMapper.findLatestOrderIdByUserNo(userNo);
            if (latestOrderId != null) {
                latestOrderDetails.put("orderInfo", paymentMapper.getOrderByOrdrNo(latestOrderId));
                latestOrderDetails.put("products", paymentMapper.getOrderedProductsByOrderId(latestOrderId));
            }
        } catch (Exception e) {
            log.error("최근 주문 상세 정보 조회 중 오류 발생: {}", e.getMessage(), e);
        }
        return latestOrderDetails;
    }
    
    @Override
    public void saveOrUpdateShippingAddress(Map<String, Object> shippingAddressData) {
        String userNo = (String) shippingAddressData.get("userNo");
        log.info("saveOrUpdateShippingAddress 호출됨. 사용자 번호: {}, 데이터: {}", userNo, shippingAddressData);
        int updatedRows = paymentMapper.updateUserInfoShippingAddress(shippingAddressData);
        if (updatedRows > 0) {
            log.info("사용자 [{}]의 배송지 정보가 users 테이블에 업데이트되었습니다.", userNo);
        } else {
            log.warn("사용자 [{}]의 배송지 정보 업데이트에 실패했거나 변경사항이 없습니다.", userNo);
        }
    }
    
    @Override
    public Map<String, Object> getShippingAddressByUserNo(String userNo) {
        log.info("getShippingAddressByUserNo 호출됨. 사용자 번호: {}", userNo);
        return paymentMapper.selectUserInfoForShipping(userNo);
    }
}