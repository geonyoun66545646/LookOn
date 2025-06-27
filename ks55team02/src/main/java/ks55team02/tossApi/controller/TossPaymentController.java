package ks55team02.tossApi.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import ks55team02.tossApi.service.TossPaymentService;
import lombok.RequiredArgsConstructor;

// 이 컨트롤러는 이제 화면(View)을 반환하지 않으므로 @RestController로 변경하는 것이 더 명확합니다.
@Controller 
@RequiredArgsConstructor
public class TossPaymentController {

    private final TossPaymentService tossPaymentService;

    // [삭제] 이 컨트롤러는 더 이상 View에 값을 전달할 필요가 없으므로 아래 필드와 메소드를 삭제합니다.
    /*
    @Value("${toss.client-key}")
    private String tossClientKey;
    
    @GetMapping("/payment")
    public String paymentPage(Model model) {
        model.addAttribute("tossClientKey", tossClientKey);
        return "customer/fragments/payment"; 
    }
    */
    
    /**
     * 결제 승인 요청을 처리하는 API 엔드포인트
     * 이 메소드는 그대로 유지합니다.
     */
    @PostMapping("/api/v1/payments/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmPayment(
            @RequestBody Map<String, String> requestData) {
        try {
            String paymentKey = requestData.get("paymentKey");
            String orderId = requestData.get("orderId");
            Long amount = Long.parseLong(requestData.get("amount"));

            Map<String, Object> result = tossPaymentService.confirmPayment(paymentKey, orderId, amount);
            
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResult = Map.of("status", "error", "message", e.getMessage());
            return new ResponseEntity<>(errorResult, HttpStatus.BAD_REQUEST);
        }
    }
}