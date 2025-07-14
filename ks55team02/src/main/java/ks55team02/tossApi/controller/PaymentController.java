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
import org.springframework.beans.factory.annotation.Value;
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
	
	// 이 필드는 그대로 유지합니다.
	@Value("${toss.client-key}")
    private String tossClientKey;

	// 장바구니
	@GetMapping(value = {"/cart"})
	public String shippmentSttsView() {
		return "customer/fragments/cart";
	}
	
	// 결제
	@GetMapping("/checkout")
	public String customerCheckOutView(Model model) {
		model.addAttribute("tossClientKey", tossClientKey);
		return "customer/fragments/checkOut";
	}

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
	        
	        // 세션에 상품 정보 저장
	        // 이 부분은 실제 쇼핑몰 로직에 따라 유연하게 변경될 수 있습니다.
	        // 여기서는 예시로 상품명과 수량을 저장합니다.
	        List<Map<String, Object>> products = (List<Map<String, Object>>) orderData.get("products");
	        session.setAttribute("orderProducts", products); // 실제 상품 정보 리스트를 세션에 저장
	        session.setAttribute("latestOrderId", orderId); // 생성된 주문 ID를 세션에 저장

	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("ordrNo", orderId); // 프론트엔드에서 orderId로 받도록 키 변경
	        return new ResponseEntity<>(response, HttpStatus.OK);
	    } catch (Exception e) {
	        log.error("주문 생성 중 오류 발생: {}", e.getMessage());
	        Map<String, Object> errorResponse = new HashMap<>();
	        errorResponse.put("success", false);
	        errorResponse.put("message", "주문 생성에 실패했습니다.");
	        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	/**
	 * 결제 성공 페이지를 렌더링하고, 결제 승인 요청을 처리합니다.
	 * Toss Payments로부터 `paymentKey`, `orderId`, `amount`를 쿼리 파라미터로 받습니다.
	 */
	@GetMapping("/payment/success")
	public String paymentSuccess(@RequestParam String paymentKey,
	                             @RequestParam String orderId,
	                             @RequestParam Long amount,
	                             HttpSession session,
	                             Model model) {
	    log.info("결제 성공 콜백 수신: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);

	    try {
	        // 1. 토스페이먼츠에 결제 승인 요청
	        Map<String, Object> paymentResult = paymentService.confirmTossPayment(paymentKey, orderId, amount);
	        log.info("토스페이먼츠 결제 승인 결과: {}", paymentResult);
	        
	        // 2. 우리 DB에 결제 정보 저장 및 주문 상태 업데이트 (PaymentServiceImpl에서 처리)
	        //    PaymentServiceImpl에서 결제 성공 시 주문 상태를 '결제 완료'로 변경하고, payment_history에 기록합니다.

	        // 3. 결제 성공 페이지에 표시할 데이터 모델에 추가
	        model.addAttribute("paymentResult", paymentResult); // 토스페이먼츠 응답 전체
	        model.addAttribute("orderId", orderId);
	        model.addAttribute("amount", amount);
	        
	        // 세션에서 상품 정보 가져와 모델에 추가
	        List<Map<String, Object>> orderProducts = (List<Map<String, Object>>) session.getAttribute("orderProducts");
	        if (orderProducts != null && !orderProducts.isEmpty()) {
	            model.addAttribute("orderProducts", orderProducts);
	            // 주문명 생성 (예: 상품명1 외 N개)
	            String orderName = orderProducts.get(0).get("name") + " 외 " + (orderProducts.size() - 1) + "개";
	            if (orderProducts.size() == 1) {
	            	orderName = (String) orderProducts.get(0).get("name");
	            }
	            model.addAttribute("orderName", orderName);
	        } else {
	        	model.addAttribute("orderName", "주문 상품");
	        }
	        
	        // (선택 사항) 세션에서 사용된 주문 관련 정보 제거
	        session.removeAttribute("orderProducts");
	        session.removeAttribute("latestOrderId");

	        return "customer/payment/success"; // payment/success.html 템플릿 렌더링
	    } catch (Exception e) {
	        log.error("결제 승인 중 오류 발생: {}", e.getMessage());
	        model.addAttribute("errorMessage", "결제 처리 중 오류가 발생했습니다: " + e.getMessage());
	        return "customer/payment/fail"; // 실패 페이지로 리다이렉트 또는 에러 페이지 렌더링
	    }
	}

	/**
	 * 결제 실패 페이지를 렌더링합니다.
	 * Toss Payments로부터 `code`, `message`, `orderId`를 쿼리 파라미터로 받습니다.
	 */
	@GetMapping("/payment/fail")
	public String paymentFail(@RequestParam String code,
	                          @RequestParam String message,
	                          @RequestParam String orderId,
	                          Model model,
	                          HttpSession session) {
	    log.error("결제 실패 콜백 수신: code={}, message={}, orderId={}", code, message, orderId);
	    model.addAttribute("errorCode", code);
	    model.addAttribute("errorMessage", message);
	    model.addAttribute("orderId", orderId);
	    
	    // 결제 실패 시 관련 세션 정보 제거
	    session.removeAttribute("orderProducts");
	    session.removeAttribute("latestOrderId");
	    
	    return "customer/payment/fail"; // payment/fail.html 템플릿 렌더링
	}
	
	/**
	 * 쿠폰 적용 요청을 처리하고 할인된 금액을 반환하는 API
	 * @param requestBody originalAmount, couponCode, (optional) userNo 포함
	 * @param session 현재 세션 (userNo 가져오기 위함)
	 * @return 할인된 금액과 성공 여부
	 */
	@PostMapping("/api/apply-coupon")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> applyCoupon(@RequestBody Map<String, Object> requestBody, HttpSession session) {
		Long originalAmount = ((Number) requestBody.get("originalAmount")).longValue();
		String couponCode = (String) requestBody.get("couponCode");
		
		// 세션에서 userNo 가져오기 (로그인한 사용자 대상)
		String userNo = null;
		if (session.getAttribute("userNo") != null) {
			userNo = (String) session.getAttribute("userNo");
		} else {
			// 사용자 ID가 없는 경우에 대한 처리 (예: 비회원 할인 불가 또는 기본 사용자)
			log.warn("쿠폰 적용 요청에 userNo가 없습니다. 비회원 처리 또는 에러.");
			userNo = "guest"; // 임시 처리
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
			response.put("message", "쿠폰 적용 중 서버 오류가 발생했습니다.");
			response.put("originalAmount", originalAmount);
			response.put("discountedAmount", originalAmount);
			response.put("discountAmount", 0);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
    /**
     * 특정 사용자가 사용 가능한 쿠폰 목록을 반환하는 API
     * @param userNo 사용자 번호 (세션에서 가져오거나 쿼리 파라미터로 받을 수 있음)
     * @return 쿠폰 목록
     */
    @GetMapping("/api/user/coupons")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getUserCoupons(@RequestParam(required = false) String userNo, HttpSession session) {
        String currentUserNo = userNo;
        // 세션에서 userNo 가져오기 (더 안전한 방법)
        if (session.getAttribute("userNo") != null) {
            currentUserNo = (String) session.getAttribute("userNo");
        } else if (currentUserNo == null || currentUserNo.isEmpty()) {
            // 로그인되지 않았거나 userNo가 제공되지 않은 경우, 빈 목록 반환 또는 오류 처리
            log.warn("쿠폰 조회 요청에 유효한 userNo가 없습니다. 빈 목록을 반환합니다.");
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }

        log.info("API 호출: 사용자 '{}'의 쿠폰 목록 조회 요청.", currentUserNo);
        try {
            List<Map<String, Object>> coupons = paymentService.getUserCoupons(currentUserNo);
            return new ResponseEntity<>(coupons, HttpStatus.OK);
        } catch (Exception e) {
            log.error("사용자 쿠폰 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	@GetMapping("/payment/orderHistory")
	public String orderHistory(Model model, HttpSession session) {
		// 실제 userNo를 세션 등에서 가져와야 합니다.
		// 여기서는 임시로 "user01"을 사용합니다.
		String userNo = (String) session.getAttribute("userNo"); 
		if(userNo == null) {
			// 로그인 되지 않은 경우 로그인 페이지로 리다이렉트
			return "redirect:/member/login"; // 로그인 페이지 URL로 수정 필요
		}
		
		List<Map<String, Object>> orderPaymentHistory = paymentService.getOrderPaymentHistoryByUser(userNo);
		model.addAttribute("orderPaymentHistory", orderPaymentHistory);
		return "customer/payment/orderHistory";
	}
	
	@GetMapping("/payment/latestOrderDetails")
	public String latestOrderDetails(Model model, HttpSession session) {
		String userNo = (String) session.getAttribute("userNo");
		if (userNo == null) {
			return "redirect:/member/login";
		}
		
		Map<String, Object> latestOrderDetails = paymentService.getLatestOrderDetailsForUser(userNo);
		
		if (latestOrderDetails != null && !latestOrderDetails.isEmpty()) {
			model.addAttribute("orderDetails", latestOrderDetails);
			return "customer/payment/latestOrderDetails";
		} else {
			// 최근 주문이 없는 경우 처리
			model.addAttribute("message", "아직 주문 내역이 없습니다.");
			return "customer/payment/noOrder"; // 또는 다른 적절한 페이지
		}
	}
	
	

}