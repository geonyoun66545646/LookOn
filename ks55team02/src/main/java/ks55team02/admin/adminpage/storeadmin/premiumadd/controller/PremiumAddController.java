package ks55team02.admin.adminpage.storeadmin.premiumadd.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/storeadmin")
public class PremiumAddController {

	@GetMapping("/premiumAdd")
	public String storeadminPremiumAddController() {
		
		return "admin/adminpage/storeadmin/premiumAdd";
	}
}
