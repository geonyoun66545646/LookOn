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
                    

                    itemData.put("storeId", product.get("store_id"));
                    
                    paymentMapper.insertOrderItem(itemData);
                }
            }

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
        
        // 1. 토스페이먼츠 API에 결제 승인을 요청합니다.
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
                throw new RuntimeException("토스페이먼츠 결제 승인 실패");
            }
        } catch (Exception e) {
            log.error("토스페이먼츠 API 호출 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("토스페이먼츠 API 호출 실패", e);
        }

        // 2. 결제 승인이 성공한 경우, 데이터베이스 업데이트를 시작합니다.
        try {
            // 2-1. 방금 결제된 주문의 모든 상품 항목을 DB에서 가져옵니다.
            List<Map<String, Object>> orderedItems = paymentMapper.getOrderedProductsByOrderId(orderId);
            // ★★★ 로그 1: DB에서 가져온 데이터 전체 확인
            log.info(">>>> [DEBUG] Ordered Items from DB: {}", orderedItems);

            // 2-2. 상점별 매출을 집계할 Map 생성
            Map<String, BigDecimal> salesByStore = new HashMap<>();
            for (Map<String, Object> item : orderedItems) {
                String storeId = (String) item.get("store_id");
                Object unitPriceObj = item.get("ordr_tm_untprc");
                Object quantityObj = item.get("ordr_qntty");
                
                // ★★★ 로그 2: 반복문 내부의 각 아이템과 클래스 타입 확인 (매우 중요!)
                log.info(">>>> [DEBUG] Processing Item - storeId: {}, unitPrice: {}, quantity: {}", storeId, unitPriceObj, quantityObj);
                if(unitPriceObj != null) log.info(">>>> [DEBUG] Price Type: " + unitPriceObj.getClass().getName());
                if(quantityObj != null) log.info(">>>> [DEBUG] Quantity Type: " + quantityObj.getClass().getName());

                // storeId가 있고, 가격과 수량이 정상적인 타입일 때만 계산
                if (storeId != null && !storeId.isEmpty() && unitPriceObj instanceof BigDecimal && quantityObj instanceof Integer) {
                    // ★★★ 로그 3: 조건문 통과 확인
                    log.info(">>>> [DEBUG] Condition PASSED for storeId: {}", storeId);
                    BigDecimal salesAmount = ((BigDecimal) unitPriceObj).multiply(new BigDecimal((Integer) quantityObj));
                    salesByStore.put(storeId, salesByStore.getOrDefault(storeId, BigDecimal.ZERO).add(salesAmount));
                } else {
                    // ★★★ 로그 4: 조건문 실패 원인 확인
                    log.warn(">>>> [DEBUG] Condition FAILED for item. Check storeId, price type, or quantity type.");
                }
            }

            // ★★★ 로그 5: 최종 집계된 상점별 매출 확인
            log.info(">>>> [DEBUG] Final Sales By Store Map: {}", salesByStore);
            
            // 2-3. 집계된 금액으로 각 상점의 총 판매 금액(tot_sel_amt)을 업데이트
            if (!salesByStore.isEmpty()) {
                log.info("상점별 매출 누적 업데이트 시작: {}", salesByStore);
                for (Map.Entry<String, BigDecimal> entry : salesByStore.entrySet()) {
                    // ★★★ 로그 6: 실제 DB 업데이트 직전 데이터 확인
                    log.info(">>>> [DEBUG] Updating DB for Store ID: {}, Amount: {}", entry.getKey(), entry.getValue());
                    paymentMapper.updateTotalSalesAmount(entry.getKey(), entry.getValue());
                }
                log.info("상점별 매출 누적 업데이트 완료.");
            } else {
                log.warn(">>>> [DEBUG] salesByStore Map is empty. No updates will be made.");
            }

            // 2-4. 주문 및 결제 상태를 업데이트합니다.
            PayOrderDTO orderDetails = paymentMapper.getOrderDetailsByOrderId(orderId);
            
            Map<String, Object> orderStatusParams = Map.of("ordrNo", orderId, "status", "PAID");
            paymentMapper.updateOrderStatus(orderStatusParams); 

            Map<String, Object> paymentUpdateParams = Map.of("pgDlngId", paymentKey, "ordrNo", orderId, "stlmSttsCd", "DONE");
            paymentMapper.updatePaymentStatusAndCompletionDate(paymentUpdateParams); 

            String paymentId = paymentMapper.getPaymentIdByPgDlngId(paymentKey);
            if(paymentId != null) {
                PaymentHistoryDTO historyDTO = new PaymentHistoryDTO();
                historyDTO.setStlmHstryId(paymentMapper.selectNextPaymentHistoryId());
                historyDTO.setStlmId(paymentId);
                paymentMapper.insertPaymentHistory(historyDTO);
            }

            // 2-5. 사용된 쿠폰 상태를 '사용됨'으로 업데이트합니다.
            if (orderDetails != null && orderDetails.getUserCpnId() != null && !orderDetails.getUserCpnId().isEmpty()) {
                paymentMapper.updateUserCouponToUsed(orderDetails.getUserCpnId()); 
                log.info("사용자 쿠폰 ({}) 상태가 '사용됨'으로 업데이트되었습니다.", orderDetails.getUserCpnId());
            }
            
            // 2-6. 장바구니에서 결제 완료된 상품을 삭제합니다.
            String userNo = (orderDetails != null) ? orderDetails.getUserNo() : null;
            if (userNo != null && !orderedItems.isEmpty()) {
                List<Map<String, Object>> itemsToDelete = new ArrayList<>();
                for(Map<String, Object> item : orderedItems) {
                    Map<String, Object> deleteInfo = new HashMap<>();
                    deleteInfo.put("gdsNo", item.get("gds_no"));
                    itemsToDelete.add(deleteInfo);
                }
                
                Map<String, Object> deleteParams = new HashMap<>();
                deleteParams.put("userNo", userNo);
                deleteParams.put("items", itemsToDelete);

                paymentMapper.deletePurchasedItemsFromCart(deleteParams);
                log.info("결제 완료된 상품들을 장바구니에서 삭제했습니다. 사용자: {}", userNo);
            }

        } catch (Exception e) {
            log.error("DB 업데이트 중 오류 발생. paymentKey: {}, orderId: {}", paymentKey, orderId, e);
            throw new RuntimeException("결제 승인 후 DB 처리 실패", e);
        }

        // 3. 모든 처리가 성공하면 토스 API 응답을 컨트롤러로 반환합니다.
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
        return paymentMapper.getUserOrderPaymentHistory(userNo); // 현재는 비어있는 리스트 반환
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