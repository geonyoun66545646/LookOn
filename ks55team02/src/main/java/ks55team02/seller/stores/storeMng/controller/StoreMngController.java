package ks55team02.seller.stores.storeMng.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StoreMngController {
	
	@GetMapping("/store/storeMng")
	public String storeMng() {
		
		
		return "seller/store/storemng/storeMngView";
	}

}
