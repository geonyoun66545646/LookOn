package ks55team02.customer.store.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/store")
@Controller
public class StoreController {

	@GetMapping("/storeMain")
	public String storeMain() {
		
		return "customer/store/storeMainView";
	}
	
	@GetMapping("/appStore")
	public String appStore(Model model) {
		
		model.addAttribute("title", "상점신청 페이지");
		return "customer/store/appStoreView";
	}
	
}
