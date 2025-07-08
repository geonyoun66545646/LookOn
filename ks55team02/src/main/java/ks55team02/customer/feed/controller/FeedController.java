package ks55team02.customer.feed.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customer/feed")
public class FeedController {

	@GetMapping("/feedList")
	public String selectFeedList() {
		return "customer/feed/feedList";
	}
	
	@GetMapping("/feedListByMe")
	public String selectFeedListByMe() {
		return "customer/feed/feedListByMe";
	}
}
