package ks55team02.customer.common.controller;

import ks55team02.seller.products.domain.Products;
import ks55team02.seller.products.service.ProductsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductCustomerHomeController {

    private final ProductsService productsService;

    @GetMapping(value = {"/customer", "/customer/"})
    public String customerHomeView() {
        return "/customer/main";
    }

    @GetMapping("/customer/products")
    public String productMainView(Model model,
                                  @RequestParam(name = "category", required = false) String category) {
        String title = "전체 상품 목록";
        String breadCrumbTitle = "상품";

        List<Products> productList;

        if (category != null && !category.isEmpty()) {
            switch (category) {
                case "best":
                    title = "베스트 상품";
                    breadCrumbTitle = "베스트";
                    // ✅ 임시로 전체 상품을 가져오도록 유지 (나중에 베스트 상품 로직으로 변경)
                    productList = productsService.getAllActiveProductsForCustomer(); 
                    break;
                case "sale":
                    title = "세일 상품";
                    breadCrumbTitle = "세일";
                    // ✅ 임시로 전체 상품을 가져오도록 유지 (나중에 세일 상품 로직으로 변경)
                    productList = productsService.getAllActiveProductsForCustomer(); 
                    break;
                case "new-products":
                    title = "신상 상품";
                    breadCrumbTitle = "신상";
                    // ✅ 임시로 전체 상품을 가져오도록 유지 (나중에 신상 상품 로직으로 변경)
                    productList = productsService.getAllActiveProductsForCustomer(); 
                    break;
                default:
                    // 알 수 없는 카테고리이거나 기본값일 경우 전체 활성 상품 조회
                    productList = productsService.getAllActiveProductsForCustomer();
                    break;
            }
        } else {
            // 카테고리 파라미터가 없을 경우 전체 활성 상품 조회
            productList = productsService.getAllActiveProductsForCustomer();
        }

        model.addAttribute("breadCrumbExists", true);
        model.addAttribute("breadCrumbTitle", breadCrumbTitle);
        model.addAttribute("title", title);
        model.addAttribute("productList", productList); 

        return "customer/productMain";
    }
}