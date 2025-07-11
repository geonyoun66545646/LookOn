package ks55team02.customer.common.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import ks55team02.seller.products.domain.Products;
import ks55team02.seller.products.service.ProductSearchService;
import lombok.RequiredArgsConstructor;

// ⭐ 1. @RequiredArgsConstructor 추가: final 필드를 위한 생성자 자동 주입
@Controller
@RequiredArgsConstructor
public class CustomerHomeController {
	
	// ⭐ 2. final 키워드 추가: 생성자 주입의 대상임을 명시
    private final ProductSearchService productSearchService;
	
	// 이 필드는 그대로 유지합니다.
	@Value("${toss.client-key}")
    private String tossClientKey;

	@GetMapping("/checkout")
	public String customerCheckOutView(Model model) {
		model.addAttribute("tossClientKey", tossClientKey);
		return "customer/fragments/checkOut";
	}
	
    // ⭐ 3. customerHomeView 메소드에 데이터 조회 로직 추가
	@GetMapping(value= {"/main","/main/"})
    public String customerHomeView(Model model, HttpServletRequest request) {
		
		model.addAttribute("currentUrl", request.getRequestURI());
		
        // --- 메인 페이지 데이터 조회 로직 ---
        
        // 1. 메인 슬라이더용 데이터 조회 (최신 상품 10개)
        List<Products> mainSlideProducts = productSearchService.getRecentProductsForMain(10);
        
        // 2. 탭별 상품 목록 데이터 조회 (각 10개씩)
        List<Products> allTabProducts = productSearchService.getAllActiveProductsForCustomer().stream().limit(10).collect(Collectors.toList());
        List<Products> newTabProducts = productSearchService.getNewProducts().stream().limit(10).collect(Collectors.toList());
        List<Products> saleTabProducts = productSearchService.getSaleProducts().stream().limit(10).collect(Collectors.toList());

        // 3. 모델에 데이터 추가
        model.addAttribute("mainSlideProducts", mainSlideProducts);
        model.addAttribute("allTabProducts", allTabProducts);
        model.addAttribute("newTabProducts", newTabProducts);
        model.addAttribute("saleTabProducts", saleTabProducts);
        
        // --- 여기까지 추가 ---
		
        return "customer/main";
    }
	
	@GetMapping(value= {"/cart"})
	public String customerCartView() {
		return "customer/fragments/cart";
	}
	
	@GetMapping(value = {"/shippmentStts"})
	public String shippmentSttsView() {
		return "customer/fragments/shippmentStts";
	}
	
	@GetMapping(value = {"/premiumAdd"})
	public String premiumAddView() {
		return "admin/fragments/premiumAdd";
	}
}