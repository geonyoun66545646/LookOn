// src/.../service/PaymentService.java

package ks55team02.tossApi.service;

import java.util.List;
import java.util.Map;

public interface PaymentService {

    /**
     * 토스페이먼츠에 결제 승인을 요청합니다.
     * @param paymentKey 토스페이먼츠에서 발급한 결제 건의 고유 키
     * @param orderId 우리가 생성한 주문 ID
     * @param amount 결제 금액
     * @return 토스페이먼츠 API로부터 받은 결제 승인 결과 데이터
     * @throws Exception API 요청 실패 시 예외 발생
     */
    Map<String, Object> confirmTossPayment(String paymentKey, String orderId, Long amount) throws Exception;
    
    /**
     * 프론트엔드에서 받은 정보로 주문을 생성합니다.
     * 이 메서드는 주문 및 초기 결제 정보를 데이터베이스에 저장하고 주문 ID를 반환합니다.
     * @param orderData 주문 관련 데이터
     * @return 생성된 주문의 ID (order_id)
     */
    String createOrder(Map<String, Object> orderData); // [수정] 주석을 더 명확하게

    /**
     * (사용하지 않을 경우 삭제 가능) 사용자 ID로 전체 주문/결제 목록을 가져옵니다.
     * @param userNo 사용자 식별자
     * @return 주문/결제 내역 리스트
     */
    List<Map<String, Object>> getOrderPaymentHistoryByUser(String userNo);
    
    /**
     * 특정 사용자의 가장 최근 주문 1건에 대한 상세 정보를 조회합니다.
     * @param userNo 사용자 식별자
     * @return 결제정보, 주문정보, 상품목록을 포함하는 Map
     */
    Map<String, Object> getLatestOrderDetailsForUser(String userNo);
    
    /**
     * 프론트엔드에서 받은 주문 데이터를 데이터베이스에 저장합니다.
     * 이 메서드는 `createOrder` 내부에서 호출되어 실제 DB 저장 로직을 수행합니다.
     * @param orderData 주문 관련 데이터 (주문 ID 포함)
     */
    void saveOrder(Map<String, Object> orderData); // [수정] 주석을 더 명확하게
    
    /**
     * 쿠폰 코드를 적용하여 할인된 최종 결제 금액을 계산합니다.
     *
     * @param originalAmount 원본 결제 금액
     * @param couponCode 적용할 쿠폰 코드
     * @param userNo 쿠폰을 사용하는 사용자 번호 (사용자별 쿠폰 유효성 검증 시 필요)
     * @return 쿠폰이 적용된 최종 할인 금액
     */
    Long calculateDiscountedAmount(Long originalAmount, String couponCode, String userNo);
    
    /**
     * 특정 사용자가 사용 가능한 쿠폰 목록을 조회합니다.
     * @param userNo 사용자 식별자
     * @return 사용 가능한 쿠폰 목록
     */
    List<Map<String, Object>> getUserCoupons(String userNo);

    /**
     * 테스트를 위해 임시로 토스페이먼츠 가짜 응답을 반환하는 메서드.
     * 실제 서비스에서는 사용하지 않습니다.
     */
    Map<String, Object> fakeConfirmTossPayment(String paymentKey, String orderId, Long amount); // [추가] PaymentServiceImpl에 있으니 인터페이스에도 추가
    
    /**
     * <<<<<<<<< [추가] 주문 ID로 주문된 상품 목록을 조회합니다. >>>>>>>>>
     * @param orderId 조회할 주문의 ID
     * @return 주문된 상품 정보 목록
     */
    List<Map<String, Object>> getOrderedProductsByOrderId(String orderId);
}