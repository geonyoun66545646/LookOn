package ks55team02.tossApi.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import ks55team02.tossApi.service.PaymentService;

@Controller
public class PaymentController {

	private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

	@Autowired
	private PaymentService paymentService;

	/**
	 * 프론트엔드에서 주문 정보를 받아 주문을 생성하고 세션에 상품 정보를 저장하는 API
	 */
	@PostMapping("/api/orders")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> orderData,
			HttpSession session) {
		log.info("주문 생성 요청 수신: {}", orderData);
		try {
			// 1. [핵심] 서비스에서 새 ID를 만들지 않고, 프론트에서 보낸 주문번호를 그대로 사용합니다.
			String orderId = (String) orderData.get("ordrNo");
			if (orderId == null || orderId.trim().isEmpty()) {
				throw new IllegalArgumentException("주문번호(ordrNo)가 요청에 포함되지 않았습니다.");
			}
			log.info("수신된 주문 ID를 그대로 사용: {}", orderId);

			// 2. 받은 데이터를 DB에 저장하는 서비스 호출 (이 서비스는 ID를 반환하지 않습니다)
			paymentService.saveOrder(orderData);

			// 3. 상품 목록을 세션에 저장
			if (orderData.containsKey("products") && orderData.get("products") instanceof List) {
				List<Map<String, Object>> products = (List<Map<String, Object>>) orderData.get("products");
				// 세션 키는 프론트에서 넘어온 orderId를 사용합니다.
				session.setAttribute(orderId + "_products", products);
				log.info("'{}'에 해당하는 상품 목록을 세션에 저장했습니다.", orderId);
			} else {
				log.warn("요청 데이터에 'products' 목록이 없습니다. 세션에 저장하지 못했습니다.");
			}

			// 4. 수신했던 주문 ID를 그대로 다시 프론트로 반환합니다.
			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("success", true);
			responseBody.put("orderId", orderId);

			return ResponseEntity.ok(responseBody);

		} catch (Exception e) {
			log.error("주문 생성 중 오류 발생", e);
			Map<String, Object> errorBody = new HashMap<>();
			errorBody.put("success", false);
			errorBody.put("message", "주문 생성 중 서버 오류가 발생했습니다: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
		}
	}

	/**
	 * (★★★ 로직 수정 ★★★) 결제 성공 시 호출되는 메서드.
	 */
	@GetMapping("/payment/success")
	public String paymentSuccess(@RequestParam String paymentKey, @RequestParam String orderId,
			@RequestParam Long amount, Model model, HttpSession session) {

		try {
			// 1. 결제 승인 로직 실행
			log.info("실제 결제 승인 로직 실행. PaymentKey: {}", paymentKey);
			Map<String, Object> paymentDetails = paymentService.confirmTossPayment(paymentKey, orderId, amount);

			// 2. 세션에서 상품 목록 가져오기
			String sessionKey = orderId + "_products";
			Object sessionData = session.getAttribute(sessionKey);

			if (sessionData instanceof List) {
				List<Map<String, Object>> orderedProducts = (List<Map<String, Object>>) sessionData;
				model.addAttribute("orderedProducts", orderedProducts);
				log.info("세션에서 상품 목록 조회 성공. Key: {}", sessionKey);
				session.removeAttribute(sessionKey);
			} else {
				log.warn("세션에서 상품 목록을 찾을 수 없습니다. Key: {}.", sessionKey);
			}

			// ★★★ 3. 모델에 "orders" 라는 이름으로 결제 정보를 담습니다. ★★★
			model.addAttribute("orders", paymentDetails);

			// 4. 결제 완료 페이지로 이동
			return "customer/fragments/paymentSuccess";

		} catch (Exception e) {
			log.error("결제 처리 중 오류 발생", e);
			model.addAttribute("errorMessage", e.getMessage());
			return "customer/fragments/paymentFail";
		}
	}

	/**
	 * 결제 실패 시 호출될 페이지
	 */
	@GetMapping("/payment/fail")
	public String paymentFail(@RequestParam(required = false) String code,
			@RequestParam(required = false) String message, @RequestParam String orderId, Model model) {

		log.warn("결제 실패. Code: {}, Message: {}, OrderId: {}", code, message, orderId);
		model.addAttribute("errorCode", code);
		model.addAttribute("errorMessage", message);

		return "customer/fragments/paymentFail";
	}

}