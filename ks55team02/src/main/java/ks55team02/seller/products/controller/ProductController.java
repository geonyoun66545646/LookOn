package ks55team02.seller.products.controller;

import ks55team02.seller.products.domain.ProductRegistrationRequest;
import ks55team02.seller.products.service.ProductsService;
import ks55team02.seller.products.domain.ProductCategory;
import ks55team02.seller.products.service.ProductCategoryService;
import ks55team02.seller.products.domain.Products;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import java.util.List;

@Controller
@RequestMapping("/seller/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductsService productsService;
    private final ProductCategoryService productCategoryService;

    @GetMapping("/registration")
    public String registerProductForm(Model model) {
        model.addAttribute("productRegistrationRequest", new ProductRegistrationRequest());
        
        // ⭐ 수정: 변경된 메소드 이름으로 호출
        List<ProductCategory> primaryProductCategories = productCategoryService.getAllTopLevelCategories();
        model.addAttribute("primaryProductCategories", primaryProductCategories);

        return "seller/products/sellerProductRegistration";
    }

    @GetMapping("/subcategories/{primaryCategoryId}")
    @ResponseBody
    public List<ProductCategory> getSubcategories(@PathVariable("primaryCategoryId") String primaryCategoryId) {
        System.out.println("2차 카테고리 요청 수신: primaryCategoryId = " + primaryCategoryId);
        
        // ⭐ 수정: 변경된 메소드 이름으로 호출
        List<ProductCategory> subcategories = productCategoryService.getSubCategoriesByParentId(primaryCategoryId);
        
        return subcategories;
    }

    @PostMapping("/register")
    public String registerProduct(@ModelAttribute ProductRegistrationRequest productRegistrationRequest,
                                  @RequestParam(value = "thumbnailImage", required = false) List<MultipartFile> thumbnailImage,
                                  @RequestParam(value = "mainImage", required = false) List<MultipartFile> mainImage,
                                  @RequestParam(value = "detailImage", required = false) List<MultipartFile> detailImage,
                                  RedirectAttributes redirectAttributes) {
        try {
            productRegistrationRequest.setThumbnailImage(thumbnailImage);
            productRegistrationRequest.setMainImage(mainImage);
            productRegistrationRequest.setDetailImage(detailImage);

            if (thumbnailImage != null && thumbnailImage.size() > 1) {
                redirectAttributes.addFlashAttribute("errorMessage", "썸네일은 1장만 업로드 가능합니다.");
                return "redirect:/seller/products/registration";
            }
            if (mainImage != null && mainImage.size() > 15) {
                redirectAttributes.addFlashAttribute("errorMessage", "대표 이미지는 최대 15장까지 업로드 가능합니다.");
                return "redirect:/seller/products/registration";
            }
            if (detailImage != null && detailImage.size() > 20) {
                redirectAttributes.addFlashAttribute("errorMessage", "상세 이미지는 최대 20장까지 업로드 가능합니다.");
                return "redirect:/seller/products/registration";
            }

            productRegistrationRequest.calculateFinalPrice();
            
            // TODO: 로그인 유저 정보로 대체할 것
            productRegistrationRequest.setStoreId("store_1");
            productRegistrationRequest.setSelUserNo("user_no_150");

            productsService.addProduct(productRegistrationRequest);

            redirectAttributes.addFlashAttribute("message", "상품이 성공적으로 등록되었습니다.");
            return "redirect:/seller/products/list";

        } catch (Exception e) {
            System.err.println("상품 등록 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "상품 등록에 실패했습니다: " + e.getMessage());
            return "redirect:/seller/products/registration";
        }
    }

    @GetMapping("/cancelRegistration")
    public String cancelProductRegistration(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "상품 등록이 취소되었습니다.");
        return "redirect:/seller/products/list";
    }

    @GetMapping("/list")
    public String myProductList(Model model) {
        String selUserNoToFilter = "user_no_150"; 
        String storeIdToFilter = "store_1";       
        
        List<Products> productList = productsService.getProductsBySellerAndStore(selUserNoToFilter, storeIdToFilter);

        model.addAttribute("productList", productList);
        return "seller/products/sellerProductList";
    }
}