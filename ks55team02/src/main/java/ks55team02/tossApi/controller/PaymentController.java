package ks55team02.tossApi.controller;

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
import ks55team02.customer.login.domain.LoginUser; 
import ks55team02.tossApi.service.PaymentService;

@Controller
public class PaymentController {

	private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

	@Autowired
	private PaymentService paymentService;
	
	@Value("${toss.client-key}")
    private String tossClientKey;
	
	/**
	 * [수정된 메서드]
	 * 이제 이 메서드는 데이터베이스를 조회하지 않습니다.
	 * 단순히 'checkout.html' 뷰 페이지만을 반환하는 역할만 합니다.
	 * 모든 데이터 표시는 해당 페이지의 JavaScript가 sessionStorage를 통해 처리합니다.
	 */
	@GetMapping("/checkout")
	public String customerCheckOutView(Model model, HttpSession session) {
	    // 1. (보안 강화) 사용자가 로그인했는지 여부만 간단히 확인합니다.
	    if (session.getAttribute("loginUser") == null) {
	        log.warn("로그인되지 않은 사용자가 /checkout 페이지에 접근하여 로그인 페이지로 리다이렉트합니다.");
	        return "redirect:/login"; // 실제 로그인 페이지 경로로 수정
	    }
	    
	    // 2. TOSS 결제에 필요한 클라이언트 키만 모델에 추가합니다.
	    model.addAttribute("tossClientKey", tossClientKey);
	    
	    // 3. Thymeleaf가 렌더링할 뷰의 경로를 반환합니다.
	    return "customer/fragments/checkOut"; // templates/customer/payment/checkOut.html 파일을 의미
	}

	/**
	 * 프론트엔드에서 주문 정보를 받아 주문을 생성하고 주문 번호를 반환하는 API
	 */
	@PostMapping("/api/orders")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> orderData, HttpSession session) {
	    log.info("주문 생성 요청 수신: {}", orderData);
	    LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
	    if (loginUser == null) {
	        return new ResponseEntity<>(Map.of("success", false, "message", "로그인이 필요합니다."), HttpStatus.UNAUTHORIZED);
	    }
	    orderData.put("userNo", loginUser.getUserNo());

	    try {
	        String orderId = paymentService.createOrder(orderData);
	        
	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("ordrNo", orderId);
	        response.put("amount", orderData.get("totalAmount"));
	        response.put("customerName", ((Map<?,?>)orderData.get("shippingAddress")).get("recipientName"));
	        
	        return new ResponseEntity<>(response, HttpStatus.OK);
	    } catch (Exception e) {
	        log.error("주문 생성 중 오류 발생: {}", e.getMessage(), e);
	        return new ResponseEntity<>(Map.of("success", false, "message", "주문 생성 실패"), HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	/**
	 * 결제 성공 페이지를 렌더링하고, 결제 승인 요청을 처리합니다.
	 */
	@GetMapping("/payment/success")
	public String paymentSuccess(@RequestParam String paymentKey,
	                             @RequestParam String orderId,
	                             @RequestParam Long amount,
	                             HttpSession session,
	                             Model model) {
	    log.info("결제 성공 콜백 수신: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);

	    try {
	        // 1단계: Toss Payments에 결제 승인 요청 (이 부분은 그대로 둡니다)
	        Map<String, Object> paymentResult = paymentService.confirmTossPayment(paymentKey, orderId, amount);
	        log.info("토스페이먼츠 결제 승인 결과 (또는 테스트 데이터): {}", paymentResult);
	        
	        // 2단계: DB에서 주문 상세 정보를 다시 조회하는 로직을 **일단 주석 처리**하여,
            // 이 부분에서 오류가 발생하는지 아닌지 확인합니다.
	        /* 
	        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
	        if(loginUser != null) {
	        	Map<String, Object> orderDetails = paymentService.getLatestOrderDetailsForUser(loginUser.getUserNo());
	        	if (orderDetails != null) {
                    model.addAttribute("orderDetails", orderDetails);
                } else {
                    log.warn("결제 성공 후 주문 상세 정보를 찾지 못했습니다. (orderId: {})", orderId);
                }
	        }
	        */

	        // 3단계: 가장 기본적인 정보만 모델에 담아서 성공 페이지로 전달합니다.
	        model.addAttribute("paymentResult", paymentResult); // Toss가 준 결과
	        model.addAttribute("orderId", orderId);             // 우리가 만든 주문 ID
	        model.addAttribute("amount", amount);               // 최종 결제 금액

	        // 성공 페이지의 뷰 이름을 반환합니다.
	        // [수정] 요청하신 경로로 변경합니다.
	        return "customer/fragments/paymentSuccess"; 

	    } catch (Exception e) {
	        log.error("결제 승인 페이지 처리 중 심각한 오류 발생: {}", e.getMessage(), e);
	        model.addAttribute("errorMessage", "결제 결과를 처리하는 중 오류가 발생했습니다. 관리자에게 문의해주세요.");
	        // [수정] 요청하신 경로로 변경합니다.
	        return "customer/fragments/paymentFail";
	    }
	}

	/**
	 * 결제 실패 페이지를 렌더링합니다.
	 */
	@GetMapping("/payment/fail")
	public String paymentFail(@RequestParam String code,
	                          @RequestParam String message,
	                          @RequestParam String orderId,
	                          Model model) {
	    log.error("결제 실패 콜백 수신: code={}, message={}, orderId={}", code, message, orderId);
	    model.addAttribute("errorCode", code);
	    model.addAttribute("errorMessage", message);
	    model.addAttribute("orderId", orderId);
	    
	    return "customer/fragments/paymentFail";
	}
	
	// 이하 다른 API 메소드들은 생략 (기존 코드 유지)
}
