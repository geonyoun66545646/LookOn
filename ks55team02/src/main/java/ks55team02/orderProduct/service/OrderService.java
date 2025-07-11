package ks55team02.orderProduct.service;

import java.util.Map;

public interface OrderService {
	/**
     * 특정 사용자의 가장 최근 주문 1건에 대한 상세 정보를 조회합니다.
     * @param userNo 사용자 식별자
     * @return 결제정보, 주문정보, 상품목록을 포함하는 Map
     */
    Map<String, Object> getLatestOrderDetailsForUser(String userNo);
    
    String createOrder(Map<String, Object> orderData);
    
    void saveOrder(Map<String, Object> orderData);
}
