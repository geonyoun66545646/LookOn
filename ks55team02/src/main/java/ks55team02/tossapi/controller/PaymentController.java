package ks55team02.tossapi.controller;

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
import ks55team02.tossapi.service.PaymentService;
// ks55team02.customer.coupons.domain.UserCoupons 임포트가 이미 존재합니다.

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
        // 이 부분은 현재 checkout.html에서 직접 사용되지 않고 있으므로, 필요하다면 활용하거나 삭제할 수 있습니다.
        Map<String, Object> latestOrderDetails = paymentService.getLatestOrderDetailsForUser(userNo);
        model.addAttribute("orderDetails", latestOrderDetails);
        model.addAttribute("tossClientKey", tossClientKey);

        return "customer/fragments/checkOut"; // Thymeleaf/JSP 뷰 파일 경로
    }

    // 2. 결제 준비 (주문 정보 DB 저장)
    @PostMapping("/api/orders")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> orderData, HttpSession session) {
        log.info("API 호출: /api/orders (주문 생성) - orderData: {}", orderData);
        
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            log.error("주문 생성 실패: 세션에 로그인 정보가 없습니다.");
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("error", "로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody);
        }
        
        String userNo = loginUser.getUserNo();
        orderData.put("userNo", userNo);
        log.info("orderData에 userNo 추가됨: {}", userNo);
        
        try {
            String orderId = paymentService.createOrder(orderData);
            log.info("주문이 성공적으로 생성되었습니다. 주문 ID: {}", orderId);
            
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("orderId", orderId);
            
            // 토스페이먼츠에 전달할 결제 금액(amount)을 할인 전 금액(totalAmount)이 아닌,
            // 최종 결제 금액(finalAmount)으로 변경합니다.
            Object finalAmountObj = orderData.get("finalAmount");
            if (finalAmountObj instanceof Integer) {
                responseBody.put("amount", ((Integer) finalAmountObj).longValue());
            } else if (finalAmountObj instanceof String) {
                responseBody.put("amount", Long.parseLong((String) finalAmountObj));
            } else if (finalAmountObj instanceof Long) {
                responseBody.put("amount", (Long) finalAmountObj);
            } else if (finalAmountObj instanceof Double) { // Double 타입으로 넘어올 경우를 대비
                responseBody.put("amount", ((Double) finalAmountObj).longValue());
            } else {
                log.warn("finalAmount의 예상치 못한 타입: {}", finalAmountObj.getClass().getName());
                responseBody.put("amount", 0L);
            }

            List<Map<String, Object>> products = (List<Map<String, Object>>) orderData.get("products");
            String orderName = "주문 상품";
            if (products != null && !products.isEmpty()) {
                String firstProductName = (String) products.get(0).get("name");
                if (products.size() > 1) {
                    orderName = firstProductName + " 외 " + (products.size() - 1) + "건";
                } else {
                    orderName = firstProductName;
                }
            }
            responseBody.put("orderName", orderName);
            responseBody.put("customerName", orderData.get("customerName"));
            
            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            log.error("주문 생성 실패: {}", e.getMessage(), e);
            Map<String, Object> errorBody = new HashMap<>();
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
            HttpSession session,
            Model model) {

        log.info("API 호출: /toss/success (결제 성공 콜백)");
        log.info("[DEBUG] Received payment success callback - paymentKey: {}, orderId: {}, amount: {}", paymentKey, orderId, amount);

        try {
            // 1. 토스 결제 승인 및 DB 상태 업데이트
            Map<String, Object> paymentResult = paymentService.confirmTossPayment(paymentKey, orderId, amount);
            
            // 2. 방금 결제 완료된 주문의 상품 목록 조회
            List<Map<String, Object>> orderedItems = paymentService.getOrderedProductsByOrderId(orderId);

            // 3. Model에 필요한 모든 데이터 추가
            model.addAttribute("paymentResult", paymentResult);
            model.addAttribute("orderId", orderId);
            model.addAttribute("ordrNo", orderId);
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

    @GetMapping("/api/user/coupons")
    @ResponseBody
    public ResponseEntity<List<ks55team02.customer.coupons.domain.UserCoupons>> getUserCoupons(HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            log.error("사용자 쿠폰 조회 실패: 세션에 로그인 정보가 없습니다.");
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unauthorized");
            error.put("message", "로그인이 필요합니다.");
            return new ResponseEntity(error, HttpStatus.UNAUTHORIZED);
        }
        String userNo = loginUser.getUserNo();
        log.info("API 호출: /api/user/coupons (사용자 쿠폰 조회) - 사용자 번호: {}", userNo);

        try {
            List<ks55team02.customer.coupons.domain.UserCoupons> userCoupons = paymentService.getActiveUserCoupons(userNo);
            return ResponseEntity.ok(userCoupons);
        } catch (Exception e) {
            log.error("쿠폰 정보를 가져오는 중 서버 에러 발생", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal Server Error");
            error.put("message", "쿠폰 정보를 가져오는 데 실패했습니다.");
            return new ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ★★★ 새로 추가할 사용자 배송지 조회 API 엔드포인트 ★★★
    @GetMapping("/api/user/shipping-info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserShippingInfo(HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            log.error("배송지 조회 실패: 세션에 로그인 정보가 없습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        String userNo = loginUser.getUserNo();
        log.info("API 호출: /api/user/shipping-info (사용자 배송지 조회) - 사용자 번호: {}", userNo);

        try {
            // PaymentService에 userNo를 인자로 받아 배송지 정보를 Map 형태로 반환하는 메소드가 필요합니다.
            // 예: public Map<String, Object> getShippingAddressByUserNo(String userNo);
            Map<String, Object> shippingInfo = paymentService.getShippingAddressByUserNo(userNo);
            if (shippingInfo != null && !shippingInfo.isEmpty()) {
                return ResponseEntity.ok(shippingInfo);
            } else {
                // 등록된 배송지가 없을 경우 204 No Content 또는 404 Not Found 반환
                // 프론트엔드에서 .ok()가 아닌 .noContent()를 확인하도록 처리해야 합니다.
                return ResponseEntity.noContent().build(); 
            }
        } catch (Exception e) {
            log.error("사용자 배송지 정보를 가져오는 중 서버 에러 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "배송지 정보를 가져오는 데 실패했습니다."));
        }
    }

    // ★★★ 새로 추가할 사용자 배송지 저장/수정 API 엔드포인트 ★★★
    @PostMapping("/api/user/shipping-info")
    @ResponseBody
    public ResponseEntity<Map<String, String>> saveUserShippingInfo(@RequestBody Map<String, Object> shippingAddressData, HttpSession session) {
        log.info("API 호출: /api/user/shipping-info (배송지 저장/수정) - shippingAddressData: {}", shippingAddressData);

        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            log.error("배송지 저장 실패: 세션에 로그인 정보가 없습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        String userNo = loginUser.getUserNo();
        shippingAddressData.put("userNo", userNo); // userNo를 데이터에 추가하여 서비스 계층으로 전달

        try {
            // PaymentService에 배송지 데이터를 받아 저장 또는 업데이트하는 메소드가 필요합니다.
            // 예: public void saveOrUpdateShippingAddress(Map<String, Object> shippingAddressData);
            paymentService.saveOrUpdateShippingAddress(shippingAddressData);
            log.info("사용자 배송지가 성공적으로 저장/업데이트되었습니다. 사용자 번호: {}", userNo);
            return ResponseEntity.ok(Map.of("message", "배송지가 성공적으로 저장되었습니다."));
        } catch (Exception e) {
            log.error("사용자 배송지 저장/업데이트 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "배송지 저장/업데이트에 실패했습니다: " + e.getMessage()));
        }
    }
}