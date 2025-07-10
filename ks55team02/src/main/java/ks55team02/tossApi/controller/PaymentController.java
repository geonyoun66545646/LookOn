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
	    Map<String, Object> response = new HashMap<>(); // 응답 맵 생성

	    try {
	        // ★★★ 수정: ordrNo를 클라이언트로부터 받지 않고, PaymentService에서 생성하도록 변경 ★★★
	        // paymentService.createOrder 메소드가 이제 생성된 ordrNo를 반환하도록 합니다.
	        String ordrNo = paymentService.createOrder(orderData); // orderData만 서비스로 전달

	        // 이전에 세션에 상품 목록을 저장하는 로직이 있었다면, 이제 생성된 ordrNo를 사용하도록 업데이트
	        // 예시: session.setAttribute(ordrNo + "_products", orderData.get("products"));
	        // (세션 사용 방식에 따라 정확한 로직은 달라질 수 있습니다.)

	        response.put("success", true);
	        response.put("ordrNo", ordrNo); // 생성된 주문번호를 클라이언트에 반환
	        log.info("주문 생성 성공. 주문번호: {}", ordrNo);
	        return new ResponseEntity<>(response, HttpStatus.OK);

	    } catch (IllegalArgumentException e) {
	        log.error("주문 생성 중 필수 파라미터 누락 또는 유효하지 않은 값: {}", e.getMessage());
	        response.put("success", false);
	        response.put("message", e.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	    } catch (Exception e) {
	        log.error("주문 생성 중 오류 발생", e);
	        response.put("success", false);
	        response.put("message", "주문 생성에 실패했습니다: " + e.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	/**
	 * (★★★ 로직 수정 ★★★) 결제 성공 시 호출되는 메서드.
	 */
	@GetMapping("/payment/success")
	public String paymentSuccess(@RequestParam String paymentKey,
	                             @RequestParam String orderId,
	                             @RequestParam Long amount,
	                             Model model,
	                             HttpSession session) {
	    log.info("결제 성공 콜백 수신. PaymentKey: {}, OrderId: {}, Amount: {}", paymentKey, orderId, amount);
	    try {
	        // 1. 토스페이먼츠 API에 결제 승인 요청
	        Map<String, Object> paymentDetails = paymentService.confirmTossPayment(paymentKey, orderId, amount);

	        // 2. 세션에서 상품 목록 가져오기 시도 (기존 로직 유지)
	        String sessionKey = orderId + "_products";
	        Object sessionData = session.getAttribute(sessionKey);

	        List<Map<String, Object>> orderedProducts = null;

	        if (sessionData instanceof List) {
	            orderedProducts = (List<Map<String, Object>>) sessionData;
	            log.info("세션에서 상품 목록 조회 성공. Key: {}", sessionKey);
	            session.removeAttribute(sessionKey); // 세션에서 상품 목록 제거
	        } else {
	            // TODO: 실제 서비스에서는 세션에 없을 경우 DB에서 주문 상세 내역을 조회하여 모델에 추가해야 합니다.
	            // 2-1. 세션에 상품 목록이 없을 경우 DB에서 주문 상세 내역 조회 (추가된 로직)
	            log.warn("세션에서 상품 목록을 찾을 수 없습니다. Key: {}. DB에서 주문 상세 내역을 조회합니다.", sessionKey);
	            Map<String, Object> latestOrderDetails = paymentService.getLatestOrderDetailsForUser(orderId); // orderId로 조회하도록 수정 필요, userNo가 아닌
	            if (latestOrderDetails != null && latestOrderDetails.containsKey("orderedProducts")) {
	                orderedProducts = (List<Map<String, Object>>) latestOrderDetails.get("orderedProducts");
	                log.info("DB에서 주문 상세 내역(상품 목록) 조회 성공. OrderId: {}", orderId);
	            } else {
	                log.error("DB에서도 주문 상세 내역(상품 목록)을 찾을 수 없습니다. OrderId: {}", orderId);
	            }
	        }
	        
	        model.addAttribute("orderedProducts", orderedProducts); // 조회된 상품 목록 모델에 추가

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
	public String paymentFail(@RequestParam(required = false) String code,
			@RequestParam(required = false) String message, @RequestParam String orderId, Model model) {

		log.warn("결제 실패. Code: {}, Message: {}, OrderId: {}", code, message, orderId);
		model.addAttribute("errorCode", code);
		model.addAttribute("errorMessage", message);

		return "customer/fragments/paymentFail";
	}
	
	/**
	 * ★★★ 여기를 추가합니다 ★★★
	 * 쿠폰 적용을 요청하여 할인된 금액을 반환하는 API 엔드포인트
	 * POST 요청으로, 원본 금액, 쿠폰 코드, 사용자 ID를 받습니다.
	 */
	@PostMapping("/api/payment/apply-coupon")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> applyCoupon(
			@RequestBody Map<String, Object> requestData,
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
			response.put("couponCode", couponCode);
			response.put("discountedAmount", discountedAmount);
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			log.error("쿠폰 적용 중 오류 발생", e);
			response.put("success", false);
			response.put("message", "쿠폰 적용 중 오류가 발생했습니다: " + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}