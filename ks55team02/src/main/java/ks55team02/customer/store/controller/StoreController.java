package ks55team02.customer.store.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StoreController {

	@GetMapping("/storeMain")
	public String storeMainView() {
		
		return "customer/store/storeMainView";
	}
}
