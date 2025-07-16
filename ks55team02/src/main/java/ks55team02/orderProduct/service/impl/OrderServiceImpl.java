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

import ks55team02.orderProduct.domain.OrderDTO;
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
        String latestOrderId = orderMapper.findLatestOrderIdByUserNo(userNo);
        if (latestOrderId == null || latestOrderId.isEmpty()) {
            return null;
        }

        // OrderDTO로 직접 매핑
        OrderDTO orderDTO = orderMapper.getCombinedOrderDetailsByOrderId(latestOrderId);
        
        // 상품 목록 조회
        List<Map<String, Object>> orderedProducts = orderMapper.getOrderedProductsByOrderId(latestOrderId);

        // 결과 맵 구성
        Map<String, Object> result = new HashMap<>();
        result.put("orderInfo", orderDTO);
        result.put("orderedProducts", orderedProducts);
        
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