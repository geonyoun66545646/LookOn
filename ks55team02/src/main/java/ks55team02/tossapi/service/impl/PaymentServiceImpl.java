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
        // createOrder는 내부적으로 saveOrder 로직을 호출하는 역할만 합니다.
        // saveOrder의 결과를 반환합니다.
        return saveOrderAndReturnId(orderData); 
    }

    // [수정] saveOrder 로직을 별도의 private 메소드로 분리하고, 생성된 주문 ID를 반환하도록 변경
    private String saveOrderAndReturnId(Map<String, Object> orderData) {
        log.info("saveOrderAndReturnId 내부 로직 시작. 전달받은 주문 데이터: {}", orderData);
        
        try {
            // 1. 주문 정보 저장 (orders 테이블)
            PayOrderDTO payorderDTO = new PayOrderDTO();
            String orderId = paymentMapper.selectNextOrderId(); 
            payorderDTO.setOrdrNo(orderId);
            payorderDTO.setUserNo((String) orderData.get("userNo"));
            // ★★★ 확인 필요 ★★★ orderData에 'orderName'이 있다면 사용. 없다면 상품 목록에서 생성하거나 기본값 설정
            payorderDTO.setGdsNm((String) orderData.get("orderName")); // orderData에서 주문 이름 가져오기
            payorderDTO.setOrdrDt(LocalDateTime.now());
            payorderDTO.setGdsTotAmt(getBigDecimalFromMap(orderData, "totalAmount"));
            payorderDTO.setApldCpnDscntAmt(getBigDecimalFromMap(orderData, "discountAmount"));
            payorderDTO.setLastStlmAmt(getBigDecimalFromMap(orderData, "finalAmount"));
            payorderDTO.setDlvyFeeAmt(BigDecimal.ZERO);
            
            // ★★★ 수정 필요 ★★★ userCpnId 키 이름 확인: "couponId"를 사용하고 있습니다.
            // checkOut.html에서 전송하는 쿠폰 ID의 키 이름과 정확히 일치하는지 확인해야 합니다.
            // 또한, "couponId"는 user_coupon_id를 의미하는지, public_coupon_id를 의미하는지도 확인 필요
            payorderDTO.setUserCpnId((String) orderData.get("couponId")); // user_coupon_id 설정
            // ★★★ 만약 필요하다면 public_coupon_id도 설정할 수 있도록 필드 추가 고려
            // payorderDTO.setPblcnCpnId((String) orderData.get("publicCouponId")); 

            payorderDTO.setOrdrSttsCd("CREATED");

            Map<String, Object> shippingAddress = (Map<String, Object>) orderData.get("shippingAddress");
            if (shippingAddress != null) {
            	payorderDTO.setRcvrNm((String) shippingAddress.get("recipientName"));
            	payorderDTO.setRcvrTelno((String) shippingAddress.get("phone")); 
            	payorderDTO.setDlvyAddr((String) shippingAddress.get("address"));
            	payorderDTO.setDlvyDaddr((String) shippingAddress.get("detailAddress")); // 상세주소 추가
            	payorderDTO.setZip((String) shippingAddress.get("postcode")); 
            	payorderDTO.setDlvyMemoCn((String) shippingAddress.get("deliveryRequest"));
            }
            // ★★★ 확인 필요 ★★★ orderData에 'customerName'이 있다면 사용
            payorderDTO.setUserName((String) orderData.get("customerName")); 

            paymentMapper.insertOrder(payorderDTO); 
            log.info("orders 테이블 저장 완료. 주문 번호: {}", orderId);

            // =========================================================================
            // ★★★ 여기가 최종 수정된 부분입니다. ★★★
            // =========================================================================
            // 2. 주문 상품 정보 저장 (order_items 테이블)
            // JSON으로 부터 List<Map<String, Object>> 형태로 products를 가져오는 것이 일반적입니다.
            List<Map<String, Object>> products = (List<Map<String, Object>>) orderData.get("products");
            if (products != null && !products.isEmpty()) {
                for (Map<String, Object> product : products) {
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("ordrDtlArtclNo", paymentMapper.selectNextOrderItemId());
                    itemData.put("ordrNo", orderId);
                    itemData.put("gdsNo", product.get("gdsNo")); // HTML에서 gdsNo로 보낼 경우
                    itemData.put("ordrQntty", product.get("quantity")); // HTML에서 quantity로 보낼 경우
                    itemData.put("ordrTmUntprc", product.get("price")); // HTML에서 price로 보낼 경우
                    itemData.put("ordrDtlArtclDcsnCd", "ORDERED");
                    
                    // ★★★ 수정 필요 ★★★ HTML에서 전송하는 store ID의 키 이름 확인.
                    // 현재 HTML에서 어떤 키 이름으로 store ID를 전송하는지 확인하여 수정해야 합니다.
                    // 예: product.get("storeId") 또는 product.get("store_id") 등
                    itemData.put("storeId", product.get("storeId")); // HTML에서 'storeId'로 보낼 경우
                    
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
            paymentDTO.setPgDlngId(orderId); // 초기에는 orderId로 PG 거래 ID를 설정할 수 있음.
            paymentDTO.setPgCoInfo("Toss Payments");
            paymentDTO.setStlmDmndDt(LocalDateTime.now());
            paymentMapper.insertPayment(paymentDTO); 

            return orderId;

        } catch (Exception e) {
            log.error("saveOrder 서비스 처리 중 심각한 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("주문 정보 저장 실패. 관리자에게 문의하세요.", e);
        }
    }

    // [추가] Map에서 값을 안전하게 BigDecimal로 변환하는 헬퍼 메소드
    private BigDecimal getBigDecimalFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return BigDecimal.ZERO;
        }
        try {
            // String, Integer, Double 등 다양한 숫자 타입을 BigDecimal로 변환
            if (value instanceof BigDecimal) {
                return (BigDecimal) value;
            } else if (value instanceof Number) {
                return new BigDecimal(value.toString());
            } else if (value instanceof String) {
                return new BigDecimal((String) value);
            }
            return BigDecimal.ZERO; // 알 수 없는 타입
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

        // amount는 Long 타입으로 넘어오므로, Map.of에 직접 사용할 수 있습니다.
        Map<String, Object> requestBody = Map.of("paymentKey", paymentKey, "orderId", orderId, "amount", amount);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        Map<String, Object> tossApiResponse;
        try {
            // responseType을 Map.class로 지정하면 LinkedHashMap으로 반환될 수 있음.
            tossApiResponse = restTemplate.postForObject(tossPaymentsUrl, requestEntity, Map.class);
            log.info("토스페이먼츠 승인 API 응답: {}", tossApiResponse);

            if (tossApiResponse == null || !"DONE".equals(tossApiResponse.get("status"))) {
                // 토스 API 응답에서 에러 메시지를 포함하도록 개선
                String errorCode = (String) tossApiResponse.get("code");
                String errorMessage = (String) tossApiResponse.get("message");
                log.error("토스페이먼츠 결제 승인 실패. 코드: {}, 메시지: {}", errorCode, errorMessage);
                throw new RuntimeException("토스페이먼츠 결제 승인 실패: " + errorMessage);
            }
        } catch (Exception e) {
            log.error("토스페이먼츠 API 호출 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("토스페이먼츠 API 호출 실패: " + e.getMessage(), e);
        }

        // 2. 결제 승인이 성공한 경우, 데이터베이스 업데이트를 시작합니다.
        try {
            // 2-1. 방금 결제된 주문의 모든 상품 항목을 DB에서 가져옵니다.
            // getOrderedProductsByOrderId는 List<Map<String, Object>>를 반환하도록 PaymentMapper.xml에 정의되어 있음.
            List<Map<String, Object>> orderedItems = paymentMapper.getOrderedProductsByOrderId(orderId);
            log.info(">>>> [DEBUG] Ordered Items from DB: {}", orderedItems);

            // 2-2. 상점별 매출을 집계할 Map 생성
            Map<String, BigDecimal> salesByStore = new HashMap<>();
            for (Map<String, Object> item : orderedItems) {
                // XML에서 AS storeId로 별칭을 지정했으므로, 여기서도 "storeId"로 접근합니다.
                String storeId = (String) item.get("storeId"); // ★★★ 수정: "store_id" -> "storeId" ★★★
                // XML에서 AS price, AS quantity로 별칭을 지정했으므로, 여기서도 "price", "quantity"로 접근합니다.
                Object unitPriceObj = item.get("price");     // ★★★ 수정: "ordr_tm_untprc" -> "price" ★★★
                Object quantityObj = item.get("quantity");   // ★★★ 수정: "ordr_qntty" -> "quantity" ★★★
                
                log.info(">>>> [DEBUG] Processing Item - storeId: {}, unitPrice: {}, quantity: {}", storeId, unitPriceObj, quantityObj);
                if(unitPriceObj != null) log.info(">>>> [DEBUG] Price Type: " + unitPriceObj.getClass().getName());
                if(quantityObj != null) log.info(">>>> [DEBUG] Quantity Type: " + quantityObj.getClass().getName());

                // BigDecimal.valueOf(Long/Double)을 사용하거나 new BigDecimal(String)을 사용하여 안전하게 변환
                BigDecimal unitPrice = BigDecimal.ZERO;
                if (unitPriceObj instanceof BigDecimal) {
                    unitPrice = (BigDecimal) unitPriceObj;
                } else if (unitPriceObj instanceof Number) {
                    unitPrice = new BigDecimal(unitPriceObj.toString());
                } else if (unitPriceObj instanceof String) {
                    unitPrice = new BigDecimal((String) unitPriceObj);
                }

                int quantity = 0;
                if (quantityObj instanceof Integer) {
                    quantity = (Integer) quantityObj;
                } else if (quantityObj instanceof Number) {
                    quantity = ((Number) quantityObj).intValue();
                } else if (quantityObj instanceof String) {
                    quantity = Integer.parseInt((String) quantityObj);
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
            
            // 2-3. 집계된 금액으로 각 상점의 총 판매 금액(tot_sel_amt)을 업데이트
            if (!salesByStore.isEmpty()) {
                log.info("상점별 매출 누적 업데이트 시작: {}", salesByStore);
                for (Map.Entry<String, BigDecimal> entry : salesByStore.entrySet()) {
                    log.info(">>>> [DEBUG] Updating DB for Store ID: {}, Amount: {}", entry.getKey(), entry.getValue());
                    // paymentMapper.updateTotalSalesAmount는 storeId와 salesAmount를 Map으로 받도록 XML에 정의되어 있어야 합니다.
                    Map<String, Object> updateSalesParams = new HashMap<>();
                    updateSalesParams.put("storeId", entry.getKey());
                    updateSalesParams.put("salesAmount", entry.getValue());
                    paymentMapper.updateTotalSalesAmount(updateSalesParams); // ★★★ 수정: Map 객체를 넘기도록 변경 ★★★
                }
                log.info("상점별 매출 누적 업데이트 완료.");
            } else {
                log.warn(">>>> [DEBUG] salesByStore Map is empty. No updates will be made.");
            }

            // 2-4. 주문 및 결제 상태를 업데이트합니다.
            // getOrderDetailsByOrderId는 PayOrderDTO를 반환하도록 PaymentMapper.xml에 정의되어 있어야 합니다.
            PayOrderDTO orderDetails = paymentMapper.getOrderDetailsByOrderId(orderId); // ★★★ 수정: getOrderDetailsByOrderId -> getOrderByOrdrNo (현재 PaymentMapper.java에 getOrderByOrdrNo가 PayOrderDTO 반환)
            
            Map<String, Object> orderStatusParams = Map.of("ordrNo", orderId, "ordrStatus", "PAID"); // ★★★ 수정: "status" -> "ordrStatus" ★★★
            paymentMapper.updateOrderStatus(orderStatusParams); 

            // PaymentMapper.xml에 updatePaymentStatusAndCompletionDate 쿼리가 있는지 확인 필요
            // 만약 없다면 PaymentMapper.xml에 PaymentDTO를 업데이트하는 쿼리 추가 필요
            // 현재 XML에는 insertPayment만 있고 updatePaymentStatusAndCompletionDate는 없음.
            // 따라서 이 부분은 주석 처리하거나, 해당 쿼리를 PaymentMapper.xml과 PaymentMapper.java에 추가해야 합니다.
            // Map<String, Object> paymentUpdateParams = Map.of("pgDlngId", paymentKey, "ordrNo", orderId, "stlmSttsCd", "DONE");
            // paymentMapper.updatePaymentStatusAndCompletionDate(paymentUpdateParams);
            
            // 결제 정보를 업데이트하는 대신, paymentMapper.insertPayment를 사용한 후
            // stlm_stts_cd 컬럼을 업데이트하는 쿼리를 PaymentMapper.xml에 추가해야 합니다.
            // 예시 쿼리 (PaymentMapper.xml에 추가 필요):
            /*
            <update id="updatePaymentStatus" parameterType="map">
                UPDATE toss_payment
                SET stlm_stts_cd = #{stlmSttsCd}, stlm_cmptn_dt = NOW(), pg_dlng_id = #{pgDlngId}
                WHERE ordr_no = #{ordrNo}
            </update>
            */
            // 그리고 PaymentMapper.java에도 해당 메소드 선언 필요:
            // void updatePaymentStatus(Map<String, Object> params);

            // ★★★ 수정/추가 필요 ★★★ 결제 상태 업데이트 로직 (위의 주석 처리된 부분 대신)
            Map<String, Object> paymentStatusUpdateParams = new HashMap<>();
            paymentStatusUpdateParams.put("ordrNo", orderId);
            paymentStatusUpdateParams.put("stlmSttsCd", "DONE");
            paymentStatusUpdateParams.put("pgDlngId", paymentKey); // 결제 키를 pg_dlng_id에 저장 (토스 API의 paymentKey)
            paymentMapper.updatePaymentStatusAndCompletionDate(paymentStatusUpdateParams); // updatePaymentStatus 메소드가 PaymentMapper에 정의되어 있다고 가정


            String paymentId = paymentMapper.getPaymentIdByPgDlngId(paymentKey);
            if(paymentId != null) {
                PaymentHistoryDTO historyDTO = new PaymentHistoryDTO();
                historyDTO.setStlmHstryId(paymentMapper.selectNextPaymentHistoryId());
                historyDTO.setStlmId(paymentId); // 결제 내역 상태 설정 (필요에 따라)
                historyDTO.setHstryCrtDt(LocalDateTime.now()); // 내역 생성 시간 설정 (필요에 따라)
                paymentMapper.insertPaymentHistory(historyDTO);
            }

            // 2-5. 사용된 쿠폰 상태를 '사용됨'으로 업데이트합니다.
            // PaymentMapper.xml의 updateUserCouponStatus 쿼리는 user_coupon_id를 파라미터로 받습니다.
            if (orderDetails != null && orderDetails.getUserCpnId() != null && !orderDetails.getUserCpnId().isEmpty()) {
                log.info("사용자 쿠폰 ({}) 상태 업데이트 시도.", orderDetails.getUserCpnId());
                paymentMapper.updateUserCouponToUsed(orderDetails.getUserCpnId()); // ★★★ 수정: updateUserCouponToUsed -> updateUserCouponStatus ★★★
                log.info("사용자 쿠폰 ({}) 상태가 '사용됨'으로 업데이트되었습니다.", orderDetails.getUserCpnId());
            } else {
                log.info("적용된 쿠폰이 없거나 userCpnId가 유효하지 않아 쿠폰 상태 업데이트를 건너뜜.");
            }
            
            // 2-6. 장바구니에서 결제 완료된 상품을 삭제합니다.
            String userNo = (orderDetails != null) ? orderDetails.getUserNo() : null;
            if (userNo != null && orderedItems != null && !orderedItems.isEmpty()) { // orderedItems가 null이거나 비어있지 않은지 확인
                List<Map<String, Object>> itemsToDelete = new ArrayList<>();
                for(Map<String, Object> item : orderedItems) {
                    Map<String, Object> deleteInfo = new HashMap<>();
                    deleteInfo.put("gdsNo", item.get("gdsNo")); // XML에서 AS gdsNo로 받아왔으므로 gdsNo 키 사용
                    itemsToDelete.add(deleteInfo);
                }
                
                Map<String, Object> deleteParams = new HashMap<>();
                deleteParams.put("userNo", userNo);
                deleteParams.put("items", itemsToDelete);

                paymentMapper.deletePurchasedItemsFromCart(deleteParams);
                log.info("결제 완료된 상품들을 장바구니에서 삭제했습니다. 사용자: {}", userNo);
            } else {
                log.warn("장바구니 삭제를 위한 userNo 또는 orderedItems가 유효하지 않습니다. userNo: {}, orderedItems: {}", userNo, orderedItems);
            }

        } catch (Exception e) {
            log.error("DB 업데이트 중 오류 발생. paymentKey: {}, orderId: {}", paymentKey, orderId, e);
            // 트랜잭션 롤백을 위해 RuntimeException을 다시 던집니다.
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
        // ★★★ 확인 필요 ★★★ 이 메소드는 paymentMapper.findUserCouponsByUserId(userNo)를 호출해야 합니다.
        // 현재 로직은 이미 그렇게 되어 있습니다.
        return paymentMapper.findUserCouponsByUserId(userNo);
    }

    // =========================================================================
    // [수정] 아래 메소드들은 현재 checkout 프로세스에서 직접 사용되지 않지만,
    // 다른 곳에서 사용될 수 있으므로 에러가 나지 않도록 수정합니다.
    // =========================================================================

    @Override
    public List<Map<String, Object>> getOrderPaymentHistoryByUser(String userNo) {
        return paymentMapper.getUserOrderPaymentHistory(userNo);
    }

    @Override
    public Map<String, Object> getLatestOrderDetailsForUser(String userNo) {
        Map<String, Object> latestOrderDetails = new LinkedHashMap<>(); // 순서 유지를 위해 LinkedHashMap 사용
        try {
            String latestOrderId = paymentMapper.findLatestOrderIdByUserNo(userNo);
            if (latestOrderId != null) {
                // getOrderDetailsByOrderId는 Map을 반환하도록 PaymentMapper.xml에 정의되어 있어야 합니다.
                // 현재 PaymentMapper.xml의 getOrderDetailsByOrderId는 Map<String, Object>를 반환합니다.
                latestOrderDetails.put("orderInfo", paymentMapper.getOrderDetailsByOrderId(latestOrderId));
                latestOrderDetails.put("products", paymentMapper.getOrderedProductsByOrderId(latestOrderId));
            }
        } catch (Exception e) {
            log.error("최근 주문 상세 정보 조회 중 오류 발생: {}", e.getMessage(), e);
        }
        return latestOrderDetails;
    }
    
    /**
     * ★★★ 추가/수정: 사용자 배송지 정보 저장 또는 업데이트 구현 ★★★
     * userNo를 사용하여 기존 배송지 정보가 있는지 확인하고, 있으면 업데이트(UPDATE), 없으면 저장(INSERT)하는 로직입니다.
     */
    @Override
    public void saveOrUpdateShippingAddress(Map<String, Object> shippingAddressData) {
        String userNo = (String) shippingAddressData.get("userNo");
        log.info("saveOrUpdateShippingAddress 호출됨. 사용자 번호: {}, 데이터: {}", userNo, shippingAddressData);
        
        // users 테이블에 해당 userNo의 주소 정보를 업데이트합니다.
        // PaymentMapper.xml에 updateUserInfoShippingAddress 쿼리가 정의되어 있어야 합니다.
        // 이 쿼리는 INSERT 또는 UPDATE 로직을 포함하지 않고 UPDATE만 수행합니다.
        // 따라서 이미 사용자 정보가 users 테이블에 존재한다는 가정을 합니다.
        int updatedRows = paymentMapper.updateUserInfoShippingAddress(shippingAddressData);
        
        if (updatedRows > 0) {
            log.info("사용자 [{}]의 배송지 정보가 users 테이블에 업데이트되었습니다.", userNo);
        } else {
            // 업데이트할 행이 없다는 것은 userNo가 users 테이블에 없거나, 데이터가 변경되지 않았다는 의미
            // 실제 시나리오에 따라 INSERT 로직을 여기에 추가하거나, users 테이블에 항상 사용자가 있다고 가정할 수 있습니다.
            log.warn("사용자 [{}]의 배송지 정보 업데이트에 실패했거나 변경사항이 없습니다. users 테이블에 해당 사용자가 존재하는지 확인하세요.", userNo);
            // 만약 INSERT 로직이 필요하다면:
            // paymentMapper.insertUserInfoShippingAddress(shippingAddressData); // 새로운 쿼리 필요
        }
    }
    
    /**
     * ★★★ 추가/수정: 사용자 배송지 정보 조회 구현 ★★★
     * paymentMapper를 통해 데이터베이스에서 사용자 배송지 정보를 조회합니다.
     */
    @Override
    public Map<String, Object> getShippingAddressByUserNo(String userNo) {
        log.info("getShippingAddressByUserNo 호출됨. 사용자 번호: {}", userNo);
        // paymentMapper를 사용하여 데이터베이스에서 해당 사용자의 배송지 정보를 조회합니다.
        // PaymentMapper.xml에 정의된 'selectUserInfoForShipping' 쿼리를 호출합니다.
        Map<String, Object> shippingInfo = paymentMapper.selectUserInfoForShipping(userNo);
        log.info("조회된 배송지 정보: {}", shippingInfo); // 디버깅을 위해 조회된 정보 로그 출력
        return shippingInfo; // 조회된 Map 데이터를 반환
    }
}