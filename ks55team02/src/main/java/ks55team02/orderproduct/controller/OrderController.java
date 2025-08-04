package ks55team02.orderproduct.controller;

import java.util.Collections;
import java.util.Map;
import java.util.List;

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

        Map<String, Object> orderData = orderService.getLatestOrderDetailsForUser(loginUser.getUserNo());

        if (orderData != null && !orderData.isEmpty()) {
            model.addAttribute("orderInfo", orderData.get("orderInfo"));
            // orderedProducts가 null인 경우를 대비하여 빈 리스트를 할당합니다.
            Object products = orderData.get("orderedProducts");
            model.addAttribute("orderedProducts", (products instanceof List) ? products : Collections.emptyList());
            model.addAttribute("paymentMethod", "신용카드");
        } else {
            model.addAttribute("message", "주문 내역이 없습니다.");
            model.addAttribute("orderInfo", null);
            model.addAttribute("orderedProducts", Collections.emptyList());
        }
        
        return "customer/fragments/paymentHistory";
    }
}