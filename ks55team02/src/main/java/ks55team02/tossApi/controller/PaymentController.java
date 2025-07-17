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
import org.springframework.web.bind.annotation.PathVariable;
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

    // 1. 결제 페이지 로드
    @GetMapping("/checkout")
    public String checkoutPage(Model model, HttpSession session) {
        if (session.getAttribute("loginUser") == null) {
            log.info("로그인되지 않은 사용자. 결제 페이지 접근 불가.");
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }

        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        String userNo = loginUser.getUserNo();
        log.info("결제 페이지 로드 요청 - 사용자 번호: {}", userNo);

        // 가장 최근 주문 상세 정보 조회 (프론트엔드에 전달)
        Map<String, Object> latestOrderDetails = paymentService.getLatestOrderDetailsForUser(userNo);
        model.addAttribute("orderDetails", latestOrderDetails);
        model.addAttribute("tossClientKey", tossClientKey);
        
        // 사용 가능한 쿠폰 목록 조회
        List<Map<String, Object>> userCoupons = paymentService.getUserCoupons(userNo);
        model.addAttribute("userCoupons", userCoupons);

        return "customer/fragments/checkout"; // Thymeleaf/JSP 뷰 파일 경로
    }

    // 2. 결제 준비 (주문 정보 DB 저장)
    @PostMapping("/api/orders")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> orderData, HttpSession session) { // Map<String, Object>로 변경
        log.info("API 호출: /api/orders (주문 생성) - orderData: {}", orderData);
        
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            log.error("주문 생성 실패: 세션에 로그인 정보가 없습니다.");
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("error", "로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody);
        }
        
     // 로그인한 사용자의 userNo를 orderData에 추가합니다.
        String userNo = loginUser.getUserNo(); // LoginUser 객체에서 사용자 번호를 가져오는 실제 메서드를 사용하세요.
                                                // 예: loginUser.getMemberId(), loginUser.getUserNo() 등
        orderData.put("userNo", userNo);
        log.info("orderData에 userNo 추가됨: {}", userNo);
        
        try {
            String orderId = paymentService.createOrder(orderData); // 주문 정보 저장 및 ID 생성
            log.info("주문이 성공적으로 생성되었습니다. 주문 ID: {}", orderId);
            
            // 토스페이먼츠 위젯에 필요한 정보들을 Map에 담아 반환합니다.
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("orderId", orderId);
            
            // `amount` (totalAmount)는 Long 타입으로 변환하여 전달
            Object totalAmountObj = orderData.get("totalAmount");
            if (totalAmountObj instanceof Integer) { // Integer로 넘어올 경우 Long으로 변환
                responseBody.put("amount", ((Integer) totalAmountObj).longValue());
            } else if (totalAmountObj instanceof String) { // String으로 넘어올 경우 Long으로 파싱
                responseBody.put("amount", Long.parseLong((String) totalAmountObj));
            } else if (totalAmountObj instanceof Long) { // 이미 Long인 경우 그대로 사용
                responseBody.put("amount", (Long) totalAmountObj);
            } else { // 그 외의 경우 (예: BigDecimal), 적절히 처리하거나 기본값 설정
                log.warn("totalAmount의 예상치 못한 타입: {}", totalAmountObj.getClass().getName());
                responseBody.put("amount", 0L); // 안전을 위해 0으로 설정하거나 예외 발생
            }

            // `orderName` (주문명)은 상품 목록에서 첫 번째 상품 이름 또는 "상품 외 N개"
            List<Map<String, Object>> products = (List<Map<String, Object>>) orderData.get("products");
            String orderName = "주문 상품"; // 기본 주문명
            if (products != null && !products.isEmpty()) {
                String firstProductName = (String) products.get(0).get("name");
                if (products.size() > 1) {
                    orderName = firstProductName + " 외 " + (products.size() - 1) + "건";
                } else {
                    orderName = firstProductName;
                }
            }
            responseBody.put("orderName", orderName);

            // `customerName` (고객명)
            responseBody.put("customerName", orderData.get("customerName"));
            
            // 필요하다면 `customerEmail`과 `customerMobilePhone`도 추가 (프론트에서 넘겨준다면)
            // responseBody.put("customerEmail", orderData.get("customerEmail"));
            // responseBody.put("customerMobilePhone", ((Map<String, Object>)orderData.get("shippingAddress")).get("phone"));


            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            log.error("주문 생성 실패: {}", e.getMessage(), e);
            Map<String, Object> errorBody = new HashMap<>(); // Map<String, Object>로 변경
            errorBody.put("error", "주문 생성에 실패했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }

    // 3. 결제 성공 처리 (토스페이먼츠 콜백)
    @GetMapping("/toss/success")
    public String handlePaymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            HttpSession session, // 세션은 현재 로직에서 직접 사용하지 않지만, 향후 확장을 위해 유지 가능합니다.
            Model model) {

        log.info("API 호출: /toss/success (결제 성공 콜백)");
        log.info("[DEBUG] Received payment success callback - paymentKey: {}, orderId: {}, amount: {}", paymentKey, orderId, amount);

        try {
            // 1. 토스 결제 승인 및 DB 상태 업데이트
            Map<String, Object> paymentResult = paymentService.confirmTossPayment(paymentKey, orderId, amount);
            
            // 2. [수정] 방금 결제 완료된 주문의 상품 목록 조회
            List<Map<String, Object>> orderedItems = paymentService.getOrderedProductsByOrderId(orderId);

            // 3. [수정] Model에 필요한 모든 데이터 추가
            model.addAttribute("paymentResult", paymentResult);
            model.addAttribute("orderId", orderId);
            model.addAttribute("amount", amount);
            model.addAttribute("orderedItems", orderedItems); // 화면에 전달할 상품 목록

            // 4. 성공 뷰 반환
            return "customer/fragments/paymentSuccess";

        } catch (Exception e) {
            log.error("결제 처리 오류: {}", e.getMessage());
            // 5. 오류 발생 시 실패 뷰와 필요한 정보 반환
            model.addAttribute("errorCode", "PAYMENT_PROCESS_ERROR");
            model.addAttribute("errorMessage", "결제 처리 중 오류 발생: " + e.getMessage());
            model.addAttribute("orderId", orderId); // 실패한 주문 ID도 함께 전달
            return "customer/fragments/paymentFail";
        }
    }
    

    // 4. 결제 실패 처리
    @GetMapping("/payment/fail")
    public String handlePaymentFail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam String orderId,
            Model model) {
        
        log.error("결제 실패 - 주문ID: {}, 코드: {}, 사유: {}", orderId, code, message);
        
        model.addAllAttributes(Map.of(
            "errorCode", code,
            "errorMessage", message,
            "orderId", orderId
        ));

        return "customer/fragments/paymentFail";
    }

    // 기타 API 엔드포인트
    @GetMapping("/api/orders/history/{userNo}")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getOrderPaymentHistory(@PathVariable String userNo) {
        log.info("API 호출: /api/orders/history/{} (주문 결제 이력 조회)", userNo);
        List<Map<String, Object>> history = paymentService.getOrderPaymentHistoryByUser(userNo);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/api/orders/latest/{userNo}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLatestOrderDetails(@PathVariable String userNo) {
        log.info("API 호출: /api/orders/latest/{} (최신 주문 상세 조회)", userNo);
        Map<String, Object> latestOrder = paymentService.getLatestOrderDetailsForUser(userNo);
        return ResponseEntity.ok(latestOrder);
    }

    @GetMapping("/api/coupons/{userNo}")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getUserCoupons(@PathVariable String userNo) {
        log.info("API 호출: /api/coupons/{} (사용자 쿠폰 조회)", userNo);
        List<Map<String, Object>> coupons = paymentService.getUserCoupons(userNo);
        return ResponseEntity.ok(coupons);
    }

    @PostMapping("/api/calculate-discount")
    @ResponseBody
    public ResponseEntity<Long> calculateDiscountedAmount(
            @RequestParam Long originalAmount,
            @RequestParam String couponCode,
            @RequestParam String userNo) {
        log.info("API 호출: /api/calculate-discount (할인 금액 계산)");
        Long discountedAmount = paymentService.calculateDiscountedAmount(originalAmount, couponCode, userNo);
        return ResponseEntity.ok(discountedAmount);
    }
}