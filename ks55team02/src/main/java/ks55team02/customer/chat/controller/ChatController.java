package ks55team02.customer.chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value="/customer")
public class ChatController {

	@GetMapping("/chat")
    public String customerChat() {
        return "customer/chat/chat"; // templates/customer/chat/chat.html
    }
	
}