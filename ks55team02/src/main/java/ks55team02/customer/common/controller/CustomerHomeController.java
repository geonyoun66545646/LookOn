package ks55team02.customer.common.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomerHomeController {
	// 이 필드는 그대로 유지합니다.
	@Value("${toss.client-key}")
    private String tossClientKey;

    // [이전 코드]
    // @GetMapping("/checkout")
    // public String showCheckoutPage(Model model) { ... }
    
    // [이전 코드]
    // @GetMapping(value= {"/checkout"})
	// public String customerCheckOutView() { ... }

    // [새로운 통합 코드] 위 두 메소드를 지우고 아래 메소드 하나만 남깁니다.
	@GetMapping("/checkout") // URL 경로는 /checkout 입니다.
	public String customerCheckOutView(Model model) { // 파라미터로 Model을 받습니다.
		
		// tossClientKey를 모델에 담아서 HTML로 전달합니다.
		model.addAttribute("tossClientKey", tossClientKey);
		
		// checkOut.html 페이지를 렌더링합니다.
		return "customer/fragments/checkOut";
	}
	
    // --- 이하 다른 메소드들은 그대로 유지합니다. ---
	@GetMapping(value= {"/main","/main/"})
    public String customerHomeView(Model model, HttpServletRequest request) {
		
		model.addAttribute("currentUrl", request.getRequestURI());
		
        return "/customer/main";
    }
	
	@GetMapping(value= {"/cart"})
	public String customerCartView() {
		return "customer/fragments/cart";
	}
	
	@GetMapping(value= {"/paymentHistory"})
	public String paymentHistoryView() {
		return "customer/fragments/paymentHistory";
	}
	
	@GetMapping(value= {"/shippmentStts"})
	public String shippmentSttsView() {
		return "customer/fragments/shippmentStts";
	}
	
	@GetMapping(value= {"/premiumAdd"})
	public String premiumAddView() {
		return "customer/fragments/premiumAdd";
	}
}