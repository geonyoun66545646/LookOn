package ks55team02.orderproduct.controller;

import java.util.Collections;
import java.util.List;
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
        // 1. 로그인 사용자 정보 확인
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
            return "redirect:/login";
        }

        // 2. 서비스 호출하여 사용자의ล่าสุด 주문 데이터 조회
        Map<String, Object> orderData = orderService.getLatestOrderDetailsForUser(loginUser.getUserNo());

        // 3. (핵심) 조회 결과에 따라 모델에 데이터 추가
        if (orderData != null && !orderData.isEmpty()) {
            // [성공] 데이터가 정상적으로 조회된 경우
            // 각 데이터를 모델에 추가합니다. get()의 결과가 null일 수도 있지만 그대로 전달합니다.
            // 뷰(HTML)에서는 th:if를 통해 null 체크를 하므로 안전합니다.
            model.addAttribute("orderInfo", orderData.get("orderInfo"));
            model.addAttribute("orderedProducts", orderData.get("orderedProducts"));
            model.addAttribute("paymentMethod", "신용카드"); // 예시 데이터

            // 만약 orderedProducts가 null로 넘어올 가능성이 있다면 아래처럼 처리하는 것이 더 안전합니다.
            // List<?> products = (List<?>) orderData.get("orderedProducts");
            // model.addAttribute("orderedProducts", (products != null) ? products : Collections.emptyList());

        } else {
            // [실패 또는 데이터 없음] 주문 내역이 없는 경우
            // 뷰(HTML)에서 오류가 발생하지 않도록 기본값을 설정해줍니다.
            model.addAttribute("message", "주문 내역이 없습니다.");
            model.addAttribute("orderInfo", null); // 또는 new OrderInfoDTO()
            model.addAttribute("orderedProducts", Collections.emptyList()); // 항상 빈 리스트를 보내 오류 방지
        }
        
        // 4. 뷰 이름 반환
        return "customer/fragments/paymentHistory";
    }
}