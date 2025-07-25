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

@Controller
@RequiredArgsConstructor
public class CustomerHomeController {
	
    private final ProductSearchService productSearchService;
	
	@GetMapping("/")
    public String customerHomeView(Model model, HttpServletRequest request) {	
		model.addAttribute("currentUrl", request.getRequestURI());
		
        // --- 메인 페이지 데이터 조회 로직 ---
        // 1. 메인 슬라이더용 데이터 조회 (최신 상품 10개)
        List<Products> mainSlideProducts = productSearchService.getRecentProductsForMain(10);
        
        // 2. ⭐ Weekly Best 상품 목록 데이터 조회 (24개)
        List<Products> weeklyBestProducts = productSearchService.getWeeklyBestProducts();
        
        // 3. ⭐ 새로 추가: 30% 이상 할인된 특가 상품 조회 및 모델에 추가
        List<Products> saleProductList = productSearchService.getSpecialSaleProducts(30.0);

        // 4. 모델에 데이터 추가
        model.addAttribute("mainSlideProducts", mainSlideProducts);
        model.addAttribute("weeklyBestProducts", weeklyBestProducts); // <-- weeklyBestProducts로 변경
        model.addAttribute("saleProductList", saleProductList);
        
        return "customer/main";
    }
	// 배송 조회 화면
	@GetMapping(value = {"/shippmentStts"})
	public String shippmentSttsView() {
		return "customer/fragments/shippmentStts";
	}
}