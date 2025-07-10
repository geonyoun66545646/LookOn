package ks55team02.tossApi.controller;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
	        String orderId = paymentService.createOrder(orderData);
	        
	        // 세션에 상품 목록 저장 (결제 성공 페이지에서 사용하기 위함)
	        List<Map<String, Object>> products = (List<Map<String, Object>>) orderData.get("products");
	        if (products != null && !products.isEmpty()) {
	            String sessionKey = orderId + "_products";
	            session.setAttribute(sessionKey, products);
	            log.info("세션에 상품 목록 저장 성공. Key: {}", sessionKey);
	        }

	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("ordrNo", orderId);
	        return new ResponseEntity<>(response, HttpStatus.OK);

	    } catch (Exception e) {
	        log.error("주문 생성 중 오류 발생", e);
	        Map<String, Object> errorResponse = new HashMap<>();
	        errorResponse.put("success", false);
	        errorResponse.put("message", "주문 생성에 실패했습니다: " + e.getMessage());
	        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	/**
	 * 토스페이먼츠 웹훅을 통해 결제 상태가 업데이트되면 호출됩니다.
	 * (이 예제에서는 사용하지 않지만, 실제 서비스에서는 필수)
	 */
	@PostMapping("/toss/webhook")
	@ResponseBody
	public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> webhookData) {
		log.info("토스페이먼츠 웹훅 수신: {}", webhookData);
		// 실제 서비스에서는 웹훅 데이터를 검증하고, DB의 주문/결제 상태를 업데이트하는 로직이 필요합니다.
		// 이 예제에서는 단순 로깅만 수행합니다.
		return new ResponseEntity<>("OK", HttpStatus.OK);
	}

	/**
	 * 결제 성공 시 리다이렉트될 페이지 (백엔드에서 결제 승인 처리)
	 */
	@GetMapping("/payment/success")
	public String paymentSuccess(@RequestParam String paymentKey,
			@RequestParam String orderId, @RequestParam Long amount, Model model,
			HttpSession session) {
		try {
			log.info("결제 성공 콜백 수신. paymentKey: {}, orderId: {}, amount: {}", paymentKey,
					orderId, amount);

			// 1. 토스페이먼츠 결제 승인 API 호출
			Map<String, Object> paymentDetails = paymentService.confirmTossPayment(paymentKey,
					orderId, amount);
			log.info("토스페이먼츠 결제 승인 응답: {}", paymentDetails);

			// 2. 세션에서 상품 목록 가져오기
			String sessionKey = orderId + "_products";
			Object sessionData = session.getAttribute(sessionKey);

			if (sessionData instanceof List) {
				List<Map<String, Object>> orderedProducts = (List<Map<String, Object>>) sessionData;
				model.addAttribute("orderedProducts", orderedProducts);
				log.info("세션에서 상품 목록 조회 성공. Key: {}", sessionKey);
				session.removeAttribute(sessionKey); // 세션에서 상품 목록 제거
			} else {
				log.warn("세션에서 상품 목록을 찾을 수 없습니다. Key: {}. 이 경우 DB에서 주문 상세 내역을 조회해야 합니다.", sessionKey);
				// TODO: 실제 서비스에서는 세션에 없을 경우 DB에서 주문 상세 내역을 조회하여 모델에 추가해야 합니다.
			}

			// ★★★ 3. 모델에 "orders" 라는 이름으로 결제 정보를 담습니다. ★★★
			model.addAttribute("orders", paymentDetails);

			// 4. 결제 완료 페이지로 이동
			return "customer/fragments/paymentSuccess"; // paymentSuccess.html이 렌더링됩니다.

		} catch (Exception e) {
			log.error("결제 처리 중 오류 발생", e);
			model.addAttribute("errorMessage", e.getMessage());
			return "customer/fragments/paymentFail"; // paymentFail.html이 렌더링됩니다.
		}
	}

	/**
	 * 결제 실패 시 호출될 페이지
	 */
	@GetMapping("/payment/fail")
	public String paymentFail(@RequestParam String code, @RequestParam String message,
			@RequestParam String orderId, Model model) {
		log.warn("결제 실패 콜백 수신. code: {}, message: {}, orderId: {}", code, message,
				orderId);
		model.addAttribute("errorCode", code);
		model.addAttribute("errorMessage", message);
		model.addAttribute("orderId", orderId);
		return "customer/fragments/paymentFail";
	}

	/**
	 * 특정 사용자의 가장 최근 주문 정보를 조회하는 API (테스트용)
	 */
	@GetMapping("/api/latest-order/{userNo}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getLatestOrderDetailsForUser(@RequestParam(name = "userNo") String userNo) {
		log.info("API 호출: 사용자 '{}'의 최근 주문 상세 내역 요청", userNo);
		try {
			Map<String, Object> latestOrderDetails = paymentService.getLatestOrderDetailsForUser(userNo);
			if (latestOrderDetails != null && !latestOrderDetails.isEmpty()) {
				log.info("최근 주문 상세 내역 조회 성공: {}", latestOrderDetails);
				return new ResponseEntity<>(latestOrderDetails, HttpStatus.OK);
			} else {
				log.warn("사용자 '{}'의 최근 주문 내역이 없습니다.", userNo);
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			log.error("사용자 '{}'의 최근 주문 상세 내역 조회 중 오류 발생", userNo, e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * 쿠폰 적용 및 할인 금액을 계산하는 API
	 */
	@PostMapping("/api/calculateDiscount")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> calculateDiscount(@RequestBody Map<String, Object> requestData,
			HttpSession session) { // 세션에서 사용자 ID를 얻거나, requestData에 포함하여 받습니다.
		
		Long originalAmount = ((Number) requestData.get("originalAmount")).longValue();
		String couponCode = (String) requestData.get("couponCode");
		// 실제 사용자 ID는 세션에서 가져오거나, 로그인 정보를 통해 얻어야 합니다.
		// 여기서는 임시로 "user001"로 설정하거나, requestData에서 받을 수 있습니다.
		String userNo = (String) session.getAttribute("userNo"); // 예시: 세션에서 사용자 ID 가져오기
		if (userNo == null) {
			userNo = (String) requestData.get("userNo"); // 요청 본문에서 사용자 ID를 받을 수도 있습니다.
			if (userNo == null) {
				// 사용자 ID가 없는 경우에 대한 처리 (예: 비회원 할인 불가 또는 기본 사용자)
				log.warn("쿠폰 적용 요청에 userNo가 없습니다. 비회원 처리 또는 에러.");
				userNo = "guest"; // 임시 처리
			}
		}

		log.info("API 호출: 쿠폰 적용 요청. 원본 금액: {}, 쿠폰 코드: {}, 사용자: {}", originalAmount, couponCode, userNo);
		
		Map<String, Object> response = new HashMap<>();
		try {
			Long discountedAmount = paymentService.calculateDiscountedAmount(originalAmount, couponCode, userNo);
			response.put("success", true);
			response.put("originalAmount", originalAmount);
			response.put("discountedAmount", discountedAmount);
			response.put("discountAmount", originalAmount - discountedAmount);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (IllegalArgumentException e) {
			log.warn("쿠폰 적용 실패: {}", e.getMessage());
			response.put("success", false);
			response.put("message", e.getMessage());
			response.put("originalAmount", originalAmount);
			response.put("discountedAmount", originalAmount); // 할인 실패 시 원본 금액 반환
			response.put("discountAmount", 0);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			log.error("쿠폰 할인 금액 계산 중 오류 발생", e);
			response.put("success", false);
			response.put("message", "할인 금액 계산 중 오류가 발생했습니다.");
			response.put("originalAmount", originalAmount);
			response.put("discountedAmount", originalAmount); // 오류 시 원본 금액 반환
			response.put("discountAmount", 0);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	
}