package ks55team02.customer.editinfo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomerEditInfo {
	
	@GetMapping("/aditinfo")
	public String ustomerEditInfoController() {
		
		return "customer/info/customerEditInfo";
	}
}
