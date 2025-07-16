package ks55team02.orderProduct.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ks55team02.orderProduct.mapper.OrderProductsMapper;
import ks55team02.orderProduct.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService{

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderProductsMapper orderMapper;

    // ... (confirmTossPayment, createOrder 등 다른 메서드 구현)

    @Override
    public String createOrder(Map<String, Object> orderData) {
        // ... 주문 생성 로직 구현 ...
        log.info("createOrder 구현 필요");
        return "order_" + UUID.randomUUID().toString().replace("-", ""); // 임시 주문 ID 생성
    }

    @Override
    public Map<String, Object> getLatestOrderDetailsForUser(String userNo) {
        log.info("사용자 '{}'의 최근 주문 상세 내역 조회를 시작합니다.", userNo);

        // 1. 가장 최근 주문 ID 조회
        String latestOrderId = orderMapper.findLatestOrderIdByUserNo(userNo);
        log.info("Step 1: 조회된 latestOrderId: {}", latestOrderId); // **로그 추가**

        // 주문 ID가 없거나 비어있으면 null 반환
        if (latestOrderId == null || latestOrderId.isEmpty()) { // 빈 문자열도 체크
            log.warn("사용자 '{}'의 주문 내역이 존재하지 않습니다. (latestOrderId is null or empty)", userNo);
            return null;
        }

        // 2. JOIN된 쿼리를 사용하여 주문/결제/배송지 정보를 한 번에 조회
        Map<String, Object> combinedDetails = orderMapper.getCombinedOrderDetailsByOrderId(latestOrderId);
        log.info("Step 2: 조회된 combinedDetails: {}", combinedDetails); // **로그 추가**

        // 3. JOIN된 쿼리를 사용하여 상품 목록을 한 번에 조회
        List<Map<String, Object>> orderedProducts = orderMapper.getOrderedProductsByOrderId(latestOrderId);
        log.info("Step 3: 조회된 orderedProducts: {}", orderedProducts); // **로그 추가**

        // **NULL 체크 및 빈 Map/List로 초기화하여 NullPointerException 방지**
        if (combinedDetails == null) {
            log.warn("combinedDetails가 null입니다. 빈 Map으로 초기화합니다.");
            combinedDetails = new HashMap<>();
        }
        if (orderedProducts == null) {
            log.warn("orderedProducts가 null입니다. 빈 List로 초기화합니다.");
            orderedProducts = new ArrayList<>(); // 빈 리스트로 초기화
        }

        // 4. 조회된 데이터를 최종 결과 Map에 담아 Controller로 반환
        Map<String, Object> result = new HashMap<>();
        result.put("paymentInfo", combinedDetails);
        result.put("orderDetails", combinedDetails); // paymentInfo와 동일한 Map 객체 사용
        result.put("orderedProducts", orderedProducts);

        log.info("최근 주문 상세 내역 조회가 완료되었습니다. 최종 결과 Map: {}", result); // **로그 추가**

        return result;
    }

    @Override
    public void saveOrder(Map<String, Object> orderData) {
        // TODO: 이 부분에 orderData Map을 OrderDTO로 변환하여
        //       Mapper를 통해 DB의 orders, order_items 테이블에 INSERT하는 로직을 구현해야 합니다.
        log.info("saveOrder 서비스 호출. orderId: {}", orderData.get("ordrNo"));
        log.warn("<<<<< saveOrder: 실제 DB 저장 로직 구현이 필요합니다. >>>>>");
    }
}