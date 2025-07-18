package ks55team02.orderproduct.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import ks55team02.customer.login.domain.LoginUser; // LoginUser 클래스 import 추가
import ks55team02.orderproduct.service.OrderService;

/**
 * 주문 내역 조회 등 주문 관련 페이지를 처리하는 컨트롤러
 */
@Controller
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService; // PaymentService가 아닌 OrderService를 주입

    /**
     * 사용자의 가장 최근 주문 상세 내역을 보여주는 페이지
     */
    @GetMapping("/paymentHistory")
    public String viewOrderHistory(Model model, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        // 서비스에서 OrderDTO와 상품 목록을 함께 반환하도록 변경
        Map<String, Object> orderData = orderService.getLatestOrderDetailsForUser(loginUser.getUserNo());
        
        if (orderData != null) {
            model.addAttribute("orderInfo", orderData.get("orderInfo")); // OrderDTO 객체
            model.addAttribute("orderedProducts", orderData.get("orderedProducts")); // 상품 목록
            model.addAttribute("paymentMethod", "신용카드"); // 결제 수단 정보 추가
        } else {
            model.addAttribute("message", "주문 내역이 없습니다.");
        }
        
        return "customer/fragments/paymentHistory";
    }
    
//    @GetMapping("/paymentHistory")
//    public String viewOrderHistory(Model model, HttpSession session) { // HttpSession 매개변수 추가
//
//        // 1. 현재 로그인한 사용자 정보 가져오기
//        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
//
//        // 로그인하지 않은 경우 처리 (예: 로그인 페이지로 리다이렉트 또는 에러 메시지 표시)
//        if (loginUser == null) {
//            log.warn("사용자 로그인 정보가 없어 주문 내역을 조회할 수 없습니다. 로그인 페이지로 리다이렉트합니다.");
//            // 로그인 페이지 URL에 따라 적절히 변경하세요.
//            model.addAttribute("message", "로그인이 필요합니다.");
//            return "customer/main"; // 예시: 로그인 페이지 경로
//            // 또는 return "redirect:/login"; // 리다이렉트도 가능
//        }
//
//        String userNoForQuery = loginUser.getUserNo(); // 로그인한 유저의 userNo 사용
//        
//        log.info("사용자 '{}'의 최근 주문 내역을 조회합니다.", userNoForQuery); // 경고 대신 정보 로그
//
//        try {
//            // 2. 서비스를 통해 가장 최근 주문의 모든 상세 정보를 가져옵니다.
//            Map<String, Object> latestOrder = orderService.getLatestOrderDetailsForUser(userNoForQuery);
//
//            // 3. HTML에서 사용할 이름으로 모델에 데이터를 담습니다.
//            if (latestOrder != null && !latestOrder.isEmpty()) {
//                // 두 번째 HTML은 paymentInfo, orderDetails, orderedProducts 라는 이름의 데이터를 기대합니다.
//                model.addAttribute("paymentInfo", latestOrder.get("paymentInfo"));
//                model.addAttribute("orderDetails", latestOrder.get("orderDetails"));
//                model.addAttribute("orderedProducts", latestOrder.get("orderedProducts"));
//            } else {
//                model.addAttribute("message", "주문 내역이 존재하지 않습니다.");
//            }
//
//            // 4. HTML 파일의 경로를 반환합니다.
//            return "customer/fragments/paymentHistory"; 
//
//        } catch (Exception e) {
//        	log.error("최근 주문 내역 조회 중 오류 발생 (사용자: {}): {}", userNoForQuery, e.getMessage());
//            log.error("예외 상세 정보:", e); // <-- 이 라인을 꼭 추가해주세요!
//            model.addAttribute("message", "주문 내역을 가져오는 중 오류가 발생했습니다: " + e.getMessage());
//            return "customer/fragments/paymentFail";
//        }
//    }
}