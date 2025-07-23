package ks55team02.tossapi.service;

import java.util.List;
import java.util.Map;

import ks55team02.customer.coupons.domain.UserCoupons;

public interface PaymentService {

    /**
     * 토스페이먼츠에 결제 승인을 요청합니다.
     */
    Map<String, Object> confirmTossPayment(String paymentKey, String orderId, Long amount) throws Exception;
    
    /**
     * 프론트엔드에서 받은 정보로 주문을 생성합니다.
     */
    String createOrder(Map<String, Object> orderData);

    /**
     * 주문 ID로 주문된 상품 목록을 조회합니다. (결제 완료 페이지에서 사용)
     */
    List<Map<String, Object>> getOrderedProductsByOrderId(String orderId);
    
    /**
     * 특정 사용자가 사용 가능한 모든 쿠폰 정보를 상세 정보(Coupon)와 함께 조회합니다.
     */
    List<UserCoupons> getActiveUserCoupons(String userNo);
    
    // =========================================================================
    // [참고] 아래 메소드들은 현재 checkout 프로세스에서 직접 사용되지 않으므로,
    // 필요 없다면 삭제하거나 이대로 두어도 무방합니다.
    // =========================================================================
    
    /**
     * 특정 사용자의 가장 최근 주문 1건에 대한 상세 정보를 조회합니다.
     */
    Map<String, Object> getLatestOrderDetailsForUser(String userNo);
    
    /**
     * 사용자 ID로 전체 주문/결제 목록을 가져옵니다.
     */
    List<Map<String, Object>> getOrderPaymentHistoryByUser(String userNo);

}