package ks55team02.tossApi.controller;

import jakarta.servlet.http.HttpSession;
import ks55team02.tossApi.service.PaymentService;
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

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestBody Map<String, Object> orderData,
            HttpSession session) {
        log.info("주문 생성 요청 수신: {}", orderData);
        try {
            String orderId = paymentService.createOrder(orderData);
            log.info("생성된 주문 ID: {}", orderId);
            
            // 주문 상품 목록을 세션에 저장
            if (orderData.get("products") instanceof List) {
                List<Map<String, Object>> products = (List<Map<String, Object>>) orderData.get("products");
                session.setAttribute(orderId + "_products", products);
                log.info("{}에 해당하는 상품 목록을 세션에 저장했습니다.", orderId);
            }

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
     * 결제 성공 시 호출되는 메서드.
     * paymentKey 유무에 따라 실제 결제 승인 또는 테스트 화면 표시를 처리합니다.
     */
    @GetMapping("/payment/success")
    public String paymentSuccess(
            @RequestParam(required = false) String paymentKey,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) Long amount,
            Model model,
            HttpSession session) {

        // 필수 파라미터인 orderId나 amount가 없으면 무조건 실패 처리
        if (orderId == null || amount == null) {
            model.addAttribute("errorMessage", "잘못된 접근입니다. 주문 정보를 찾을 수 없습니다.");
            return "customer/fragments/paymentFail";
        }

        Map<String, Object> paymentDetails;

        try {
            if (paymentKey != null) {
                // [실제 운영/Postman 테스트] paymentKey가 있으면, 실제 결제 승인 로직 실행
                log.info("실제 결제 승인 로직 실행. PaymentKey: {}", paymentKey);
                paymentDetails = paymentService.confirmTossPayment(paymentKey, orderId, amount);
            } else {
                // [JavaScript 테스트 모드] paymentKey가 없으면, 토스 API 호출을 건너뛰고 화면 표시용 가짜 데이터 생성
                log.warn("<<<<< paymentKey가 없어 테스트 모드로 처리합니다. (DB 저장 안됨) >>>>>");
                paymentDetails = new HashMap<>();
                paymentDetails.put("orderId", orderId);
                paymentDetails.put("amount", amount);
                paymentDetails.put("method", "테스트 카드");
                paymentDetails.put("status", "DONE (테스트)");
                paymentDetails.put("approvedAt", OffsetDateTime.now(ZoneId.of("Asia/Seoul")));
            }

            // --- 세션에서 실제 주문 상품 목록을 가져오는 공통 로직 ---
            String sessionKey = orderId + "_products";
            Object sessionData = session.getAttribute(sessionKey);
            if (sessionData instanceof List) {
                List<Map<String, Object>> orderedProducts = (List<Map<String, Object>>) sessionData;
                model.addAttribute("orderedProducts", orderedProducts);

                // 테스트 모드일 때, 실제 상품명으로 주문명(orderName)을 만들어주기
                if (paymentKey == null && !orderedProducts.isEmpty()) {
                    String realOrderName = orderedProducts.size() > 1
                        ? (String) orderedProducts.get(0).get("name") + " 외 " + (orderedProducts.size() - 1) + "건"
                        : (String) orderedProducts.get(0).get("name");
                    paymentDetails.put("orderName", realOrderName);
                }
                
                // 실제 결제(paymentKey가 있는 경우)가 아니면 세션을 굳이 지우지 않아도 됨
                // 혹은 테스트 반복을 위해 지우지 않는 것이 나을 수 있음
                if (paymentKey != null) {
                    session.removeAttribute(sessionKey);
                }
            }
            
            model.addAttribute("payment", paymentDetails);
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
    public String paymentFail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam String orderId,
            Model model) {
        
        log.warn("결제 실패. Code: {}, Message: {}, OrderId: {}", code, message, orderId);
        model.addAttribute("errorCode", code);
        model.addAttribute("errorMessage", message);

        return "customer/fragments/paymentFail";
    }
}