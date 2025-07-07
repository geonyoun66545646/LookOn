package ks55team02.customer.coupons.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value="/customer")
public class CouponsController {

	@GetMapping("/coupons")
    public String customerCoupons() {
        return "customer/coupons/coupons"; // templates/customer/coupons/coupons.html
    }
	
}
