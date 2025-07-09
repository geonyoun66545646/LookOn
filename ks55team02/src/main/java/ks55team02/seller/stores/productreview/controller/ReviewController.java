package ks55team02.seller.stores.productreview.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReviewController {
	
	@GetMapping("/review")
	public String Review() {
		
		return "seller/store/productReview/productReview.html";
	}

}
