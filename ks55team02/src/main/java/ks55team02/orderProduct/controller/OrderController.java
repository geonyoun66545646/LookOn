package ks55team02.orderProduct.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; // @RestController가 아닌 @Controller를 사용합니다.
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ks55team02.orderProduct.service.OrderService;

/**
 * 주문 내역 조회 등 주문 관련 페이지를 처리하는 컨트롤러
 */
@Controller // HTML 페이지를 반환하므로 @Controller를 사용합니다.
public class OrderController {
	
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService; // PaymentService가 아닌 OrderService를 주입

    /**
     * 사용자의 가장 최근 주문 상세 내역을 보여주는 페이지
     */
    @GetMapping("/paymentHistory")
    public String viewOrderHistory(Model model) {
    	
        // 1. 테스트를 위해 사용자 번호를 직접 지정합니다.
        // TODO: 실제 로그인 기능 구현 후 Spring Security에서 사용자 정보를 가져와야 합니다.
        String userNoForQuery = "temp-user-01";
        log.warn("<<<<< 테스트 모드: 사용자 '{}'의 최근 주문 내역을 조회합니다. >>>>>", userNoForQuery);

        try {
            // 2. 서비스를 통해 가장 최근 주문의 모든 상세 정보를 가져옵니다.
            Map<String, Object> latestOrder = orderService.getLatestOrderDetailsForUser(userNoForQuery);

            // 3. HTML에서 사용할 이름으로 모델에 데이터를 담습니다.
            if (latestOrder != null && !latestOrder.isEmpty()) {
                // 두 번째 HTML은 paymentInfo, orderDetails, orderedProducts 라는 이름의 데이터를 기대합니다.
                model.addAttribute("paymentInfo", latestOrder.get("paymentInfo"));
                model.addAttribute("orderDetails", latestOrder.get("orderDetails"));
                model.addAttribute("orderedProducts", latestOrder.get("orderedProducts"));
            } else {
                model.addAttribute("message", "주문 내역이 존재하지 않습니다.");
            }

            // 4. 두 번째 HTML 파일의 경로를 반환합니다.
            return "customer/fragments/paymentHistory"; 

        } catch (Exception e) {
            log.error("최근 주문 내역 조회 중 오류 발생 (사용자: {})", userNoForQuery, e);
            model.addAttribute("errorMessage", "주문 정보를 불러오는 데 실패했습니다.");
            return "customer/fragments/paymentFail"; 
        }
    }
}