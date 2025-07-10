// src/.../service/PaymentServiceImpl.java

package ks55team02.tossApi.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.tossApi.mapper.PaymentMapper; // Mapper의 실제 경로에 맞게 수정하세요
import ks55team02.tossApi.service.PaymentService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional // DB 작업을 하므로 트랜잭션 처리를 권장합니다.
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    // MyBatis Mapper를 주입받습니다.
    @Autowired
    private PaymentMapper paymentMapper;
    
    
    @Override
    public void saveOrder(Map<String, Object> orderData) {
        // TODO: 이 부분에 orderData Map을 OrderDTO로 변환하여
        //       Mapper를 통해 DB의 orders, order_items 테이블에 INSERT하는 로직을 구현해야 합니다.
        log.info("saveOrder 서비스 호출. orderId: {}", orderData.get("ordrNo"));

        // orders 테이블에 데이터 삽입 (PaymentMapper.xml에 insertOrder 쿼리 필요)
        // 예시:
        // paymentMapper.insertOrder(orderData); // orderData 맵을 직접 넘기거나 DTO로 변환하여 넘깁니다.

        // order_items 테이블에 데이터 삽입 (PaymentMapper.xml에 insertOrderItem 쿼리 필요)
        // orderData.get("products") 등으로 상품 목록을 가져와 반복문을 돌며 저장
        // List<Map<String, Object>> products = (List<Map<String, Object>>) orderData.get("products");
        // if (products != null) {
        //     for (Map<String, Object> product : products) {
        //         product.put("ordrNo", orderData.get("ordrNo")); // 주문 상품에도 주문번호 설정
        //         paymentMapper.insertOrderItem(product); // 각 상품 정보를 저장
        //     }
        // }

        // 여기에 DB 저장 로직을 실제 구현해야 합니다. (Mapper 호출)
        // 예시:
        Map<String, Object> order = new HashMap<>();
        order.put("ordrNo", orderData.get("ordrNo"));
        order.put("totalAmount", orderData.get("totalAmount"));
        order.put("userNo", orderData.get("userNo")); // 사용자 ID가 orderData에 있어야 함
        order.put("orderStatus", "ORDER_COMPLETED"); // 주문 상태 코드
        order.put("orderDate", ZonedDateTime.now(ZoneId.of("Asia/Seoul"))); // 현재 시간
        
        // paymentMapper.insertOrder(order); // 주문 테이블에 삽입하는 Mapper 메소드 호출
        log.info("주문 데이터 저장 로직 구현 필요: {}", order);

        List<Map<String, Object>> products = (List<Map<String, Object>>) orderData.get("products");
        if (products != null) {
            for (Map<String, Object> product : products) {
                product.put("ordrNo", orderData.get("ordrNo"));
                // paymentMapper.insertOrderItem(product); // 주문 상품 테이블에 삽입하는 Mapper 메소드 호출
                log.info("주문 상품 데이터 저장 로직 구현 필요: {}", product);
            }
        }
    }
    
    
    
    // --- 기존에 있던 메서드 구현부 (예시) ---

    /**
     * 결제 승인 처리 및 화면에 표시할 데이터를 생성합니다.
     */
    @Override
    public Map<String, Object> confirmTossPayment(String paymentKey, String orderId, Long amount) throws Exception {
        log.warn("<<<<< confirmTossPayment: 실제 API 호출 대신 가짜 데이터를 생성합니다. >>>>>");

        Map<String, Object> paymentDetails = new HashMap<>();
        
        paymentDetails.put("orderId", orderId);
        paymentDetails.put("amount", amount);
        paymentDetails.put("method", "테스트 카드");
        paymentDetails.put("approvedAt", ZonedDateTime.now(ZoneId.of("Asia/Seoul")));
        
        return paymentDetails;
    }

    @Override
    public String createOrder(Map<String, Object> orderData) {
        log.info("PaymentService.createOrder 호출. 받은 데이터: {}", orderData);

        // ★★★ 주문번호(ordrNo) 서버에서 생성 ★★★
        // 기존: String ordrNo = UUID.randomUUID().toString(); // 고유한 주문번호 생성 예시
        // 변경: PaymentMapper를 통해 DB에서 다음 주문 번호를 가져옵니다.
        String ordrNo = paymentMapper.selectNextOrderId(); // <--- 이 부분을 이렇게 수정합니다.
        log.info("생성된 새로운 주문 번호: {}", ordrNo); // 생성된 주문 번호 로그 추가

        orderData.put("ordrNo", ordrNo); // 생성된 주문번호를 orderData에 추가

        // 사용자 번호 (userNo)는 로그인된 사용자 세션에서 가져와야 할 수 있습니다.
        // 현재 orderData에 userNo가 없다면, 클라이언트에서 보내주거나
        // 서버에서 사용자 인증 정보를 통해 가져와서 orderData에 추가해야 합니다.
        // 예시: String userNo = (String) session.getAttribute("userNo");
        // orderData.put("userNo", userNo);

        // ★★★ orderData에서 필요한 정보를 추출하여 OrderDTO 등으로 변환 후 DB에 저장 ★★★
        // 이전에 언급된 saveOrder 메소드를 활용하거나 여기에 직접 구현합니다.
        try {
            saveOrder(orderData); // 주문 정보 및 상품 목록을 DB에 저장하는 로직 호출
            log.info("주문번호 {}로 주문 생성 및 DB 저장 완료.", ordrNo);
        } catch (Exception e) {
            log.error("주문번호 {} DB 저장 중 오류 발생: {}", ordrNo, e.getMessage(), e);
            throw new RuntimeException("주문 저장 중 오류 발생", e); // 예외 다시 던지기
        }

        return ordrNo; // 생성된 주문번호 반환
    }

    @Override
    public List<Map<String, Object>> getOrderPaymentHistoryByUser(String userNo) {
        // ... 전체 주문 목록 조회 로직 구현 ...
        log.info("getOrderPaymentHistoryByUser 구현 필요");
        return null; // 임시 반환
    }

    // --- ▼▼▼ 여기에 새로 추가된 메서드의 실제 구현부 ▼▼▼ ---

    /**
     * (★★★ 여기를 구현했습니다 ★★★)
     * 특정 사용자의 가장 최근 주문 상세 정보를 조회합니다.
     * @param userNo 사용자 식별자
     * @return 결제정보, 주문정보, 상품목록을 포함하는 Map
     */
    @Override
    public Map<String, Object> getLatestOrderDetailsForUser(String userNo) {
        log.info("사용자 '{}'의 최근 주문 상세 내역 조회를 시작합니다.", userNo);

        // 1. Mapper를 통해 DB에서 해당 사용자의 가장 최근 order_id를 조회합니다.
        String latestOrderId = paymentMapper.findLatestOrderIdByUserNo(userNo);

        // 주문 내역이 없는 경우
        if (latestOrderId == null) {
            log.warn("사용자 '{}'의 주문 내역이 존재하지 않습니다.", userNo);
            return null; 
        }
        log.info("조회된 최근 주문 ID: {}", latestOrderId);

        // 2. 찾은 order_id를 사용해 각 상세 정보를 조회합니다.
        //    (이 메서드들은 PaymentMapper에 각각 정의되어 있어야 합니다.)
        Map<String, Object> paymentInfo = paymentMapper.getPaymentDetailsByOrderId(latestOrderId);
        Map<String, Object> orderDetails = paymentMapper.getOrderDetailsByOrderId(latestOrderId);
        List<Map<String, Object>> orderedProducts = paymentMapper.getOrderedProductsByOrderId(latestOrderId);

        // 3. 모든 정보를 하나의 Map에 담아서 컨트롤러로 반환합니다.
        Map<String, Object> result = new HashMap<>();
        result.put("paymentInfo", paymentInfo);
        result.put("orderDetails", orderDetails);
        result.put("orderedProducts", orderedProducts);

        log.info("최근 주문 상세 내역 조회가 완료되었습니다. (orderId: {})", latestOrderId);
        
        return result;
    }
    
    /**
     * ★★★ 여기를 구현합니다 ★★★
     * 쿠폰 코드를 적용하여 할인된 최종 결제 금액을 계산합니다.
     * 실제 쿠폰 조회 및 유효성 검증 로직이 들어갑니다.
     * @param originalAmount 원본 결제 금액
     * @param couponCode 적용할 쿠폰 코드
     * @param userNo 쿠폰을 사용하는 사용자 번호 (사용자별 쿠폰 유효성 검증 시 필요)
     * @return 쿠폰이 적용된 최종 할인 금액
     */
    @Override
    public Long calculateDiscountedAmount(Long originalAmount, String couponCode, String userNo) {
        if (couponCode == null || couponCode.isEmpty()) {
            log.info("쿠폰 코드가 제공되지 않았습니다. 원본 금액 {} 반환.", originalAmount);
            return originalAmount;
        }

        // 1. PaymentMapper를 통해 사용자의 쿠폰 상세 정보를 조회합니다.
        Map<String, Object> couponDetails = paymentMapper.getUserCouponDetails(userNo, couponCode);

        Long discountedAmount = originalAmount; // 기본값은 원본 금액

        if (couponDetails != null && (Boolean) couponDetails.get("useYn")) { // 사용 가능한 쿠폰인지 확인
            String discountType = (String) couponDetails.get("dscntTpCd"); // 할인 유형 (RATE 또는 FIXED)
            Number discountValueNum = (Number) couponDetails.get("dscntVl"); // 할인 값
            Long minOrderAmount = ((Number) couponDetails.get("minOrdrAmt")).longValue(); // 최소 주문 금액
            Long maxDiscountAmount = ((Number) couponDetails.get("maxDscntAmt")).longValue(); // 최대 할인 금액

            // 최소 주문 금액 조건을 만족하는지 확인
            if (originalAmount < minOrderAmount) {
                log.warn("쿠폰 '{}'은 최소 주문 금액({}원) 미만으로 적용할 수 없습니다. 현재 금액: {}원", couponCode, minOrderAmount, originalAmount);
                return originalAmount; // 쿠폰 적용 없이 원본 금액 반환
            }

            if (discountType != null && discountValueNum != null) {
                if ("RATE".equals(discountType)) { // 비율 할인
                    double discountRate = discountValueNum.doubleValue();
                    long discount = (long) (originalAmount * (discountRate / 100.0));
                    
                    // 최대 할인 금액 제한 적용
                    if (maxDiscountAmount > 0 && discount > maxDiscountAmount) {
                        discount = maxDiscountAmount;
                    }
                    discountedAmount = originalAmount - discount;
                } else if ("FIXED".equals(discountType)) { // 고정 금액 할인
                    long discount = discountValueNum.longValue();
                    
                    // 최대 할인 금액 제한은 고정 금액 할인에서는 일반적으로 사용되지 않으나, 정책에 따라 추가 가능
                    // if (maxDiscountAmount > 0 && discount > maxDiscountAmount) {
                    //     discount = maxDiscountAmount;
                    // }
                    discountedAmount = originalAmount - discount;
                }
            } else {
                log.warn("쿠폰 '{}'의 할인 유형 또는 할인 값이 유효하지 않습니다. 원본 금액 반환.", couponCode);
            }
        } else {
            log.warn("쿠폰 '{}'을 찾을 수 없거나 유효하지 않습니다. 원본 금액 반환.", couponCode);
        }

        // 최종 금액이 0보다 작아지지 않도록 처리
        if (discountedAmount < 0) {
            discountedAmount = 0L;
        }
        
        log.info("쿠폰 '{}' 적용 완료. 원본 금액: {}, 최종 금액: {}", couponCode, originalAmount, discountedAmount);
        return discountedAmount;
    }

}