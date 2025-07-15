package ks55team02.customer.coupons.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping(value="/customer")
@Slf4j
public class CouponsController {
    
    /**
     * 사용자 쿠폰함 페이지(뷰)를 반환합니다.
     * @return templates/customer/coupons/coupons.html
     */
    @GetMapping("/coupons")
    public String customerCouponsPage() {
        log.info("사용자 쿠폰함 페이지 요청");
		return "customer/coupons/coupons";
	}
}