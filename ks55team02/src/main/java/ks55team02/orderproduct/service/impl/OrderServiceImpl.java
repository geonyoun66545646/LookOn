package ks55team02.orderproduct.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ks55team02.orderproduct.domain.OrderDTO;
import ks55team02.orderproduct.mapper.OrderProductsMapper;
import ks55team02.orderproduct.service.OrderService;

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

        // 1. Mapper를 호출하여 사용자의 가장 최근 주문 번호(ordrNo)를 가져옵니다.
        String latestOrderNo = orderMapper.findLatestOrderIdByUserNo(userNo);

        // 2. 만약 사용자의 주문 기록이 없다면, null을 반환하여 데이터가 없음을 알립니다.
        if (latestOrderNo == null) {
            System.out.println("서비스: " + userNo + " 님의 주문 내역이 없습니다.");
            return null; // 컨트롤러에서 null 체크를 통해 후속 처리를 합니다.
        }

        // 3. 조회된 주문 번호를 사용하여 주문의 기본/결제/배송 정보를 가져옵니다.
        OrderDTO orderInfo = orderMapper.getCombinedOrderDetailsByOrderId(latestOrderNo);

        // 4. 동일한 주문 번호로 주문된 상품 목록을 가져옵니다.
        List<Map<String, Object>> orderedProducts = orderMapper.getOrderedProductsByOrderId(latestOrderNo);

        // [디버깅 로그] 각 단계에서 데이터가 제대로 조회되었는지 확인하는 것이 매우 중요합니다.
        System.out.println("--- OrderServiceImpl 데이터 확인 ---");
        System.out.println("사용자 최근 주문 번호: " + latestOrderNo);
        System.out.println("조회된 주문 기본 정보 (orderInfo): " + orderInfo);
        System.out.println("조회된 주문 상품 목록 (orderedProducts): " + orderedProducts);
        System.out.println("----------------------------------");

        // 5. 조회된 두 종류의 데이터를 컨트롤러에서 사용하기 좋게 하나의 Map으로 묶습니다.
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("orderInfo", orderInfo);
        resultData.put("orderedProducts", orderedProducts);

        // 6. 데이터가 담긴 Map을 컨트롤러로 반환합니다.
        return resultData;
    }

    @Override
    public void saveOrder(Map<String, Object> orderData) {
        // TODO: 이 부분에 orderData Map을 OrderDTO로 변환하여
        //       Mapper를 통해 DB의 orders, order_items 테이블에 INSERT하는 로직을 구현해야 합니다.
        log.info("saveOrder 서비스 호출. orderId: {}", orderData.get("ordrNo"));
        log.warn("<<<<< saveOrder: 실제 DB 저장 로직 구현이 필요합니다. >>>>>");
    }
}