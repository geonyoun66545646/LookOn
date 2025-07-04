package ks55team02.tossApi.service;

import java.util.Map;

public interface PaymentService {
	/*
	 * Map<String, Object> confirmPayment(Map<String, String> paymentData) throws
	 * Exception;
	 */
	 
	    /**
	     * 토스페이먼츠에 결제 승인을 요청합니다.
	     * @param paymentKey 토스페이먼츠에서 발급한 결제 건의 고유 키
	     * @param orderId 우리가 생성한 주문 ID
	     * @param amount 결제 금액
	     * @return 토스페이먼츠 API로부터 받은 결제 승인 결과 데이터
	     * @throws Exception API 요청 실패 시 예외 발생
	     */
	Map<String, Object> confirmTossPayment(String paymentKey, String orderId, Long amount) throws Exception;
	
	// 리턴 타입: String
    // 파라미터 타입: Map<String, Object>
    String createOrder(Map<String, Object> orderData);
}

