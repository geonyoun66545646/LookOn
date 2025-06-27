package ks55team02.tossApi.service;

import java.util.Map;

public interface TossPaymentService {
	Map<String, Object> confirmPayment(String paymentKey, String orderId, Long amount);
}
