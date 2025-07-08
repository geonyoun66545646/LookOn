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
        log.warn("<<<<< saveOrder: 실제 DB 저장 로lic 구현이 필요합니다. >>>>>");
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
        // ... 주문 생성 로직 구현 ...
        log.info("createOrder 구현 필요");
        return "order_" + UUID.randomUUID().toString().replace("-", ""); // 임시 주문 ID 생성
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
}