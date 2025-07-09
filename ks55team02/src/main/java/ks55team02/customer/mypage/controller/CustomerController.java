package ks55team02.customer.mypage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomerController {
	
	@GetMapping("/customer/mypage")
	public String CustomerMyPageController() {
		
		return "customer/mypage/customerMyPage";
	}
}
