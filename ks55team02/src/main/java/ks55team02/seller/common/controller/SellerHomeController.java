package ks55team02.seller.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SellerHomeController {
	
	@GetMapping(value= {"/premiumCheckout"})
	public String customerPremiumCheckoutView() {
		
		return "seller/fragments/premiumCheckout";
	}
	
	@GetMapping(value= {"/premiumSubscription"})
	public String customerPremiumSubscriptionView() {
		
		return "seller/fragments/premiumSubscription";
	}
	
	@GetMapping(value= {"/premiumCheckoutHistory"})
	public String premiumCheckoutHistoryView() {
		
		return "seller/fragments/premiumCheckoutHistory";
	}
	

	
	
	


}
