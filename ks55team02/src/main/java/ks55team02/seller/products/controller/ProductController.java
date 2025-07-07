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

    // 상품 등록 폼
    @GetMapping("/registration")
    public String registerProductForm(Model model) {
        model.addAttribute("productRegistrationRequest", new ProductRegistrationRequest());
        List<ProductCategory> primaryProductCategories = productCategoryService.getAllTopLevelCategories();
        model.addAttribute("primaryProductCategories", primaryProductCategories);
        return "seller/products/sellerProductRegistration";
    }

    // 상품 등록 처리
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

    // 상품 수정 폼
    @GetMapping("/update/{gdsNo}")
    public String updateProductForm(@PathVariable("gdsNo") String gdsNo, Model model) {
        Products product = productsService.getProductDetailWithImages(gdsNo);
        if(product == null) {
            return "redirect:/error/404";
        }

        ProductRegistrationRequest requestDto = new ProductRegistrationRequest();
        requestDto.setGdsNo(product.getGdsNo());
        requestDto.setProductName(product.getGdsNm());
        requestDto.setProductDescription(product.getGdsExpln());
        requestDto.setBasePrice(new java.math.BigDecimal(product.getBasPrc()).longValue());
        requestDto.setDiscountRate(product.getDscntRt());
        requestDto.setMinPurchase(product.getMinPurchaseQty());
        requestDto.setMaxPurchase(product.getMaxPurchaseQty());
        
        ProductCategory categoryInfo = productCategoryService.getCategoryById(product.getCtgryNo());
        if(categoryInfo != null) {
            if(categoryInfo.getCategoryLevel() == 2) {
                requestDto.setProductCategory2(categoryInfo.getCategoryId());
                requestDto.setProductCategory1(categoryInfo.getParentCategoryId());
            } else {
                requestDto.setProductCategory1(categoryInfo.getCategoryId());
            }
        }
        
        model.addAttribute("productData", product);
        model.addAttribute("productRegistrationRequest", requestDto);
        model.addAttribute("isUpdate", true);

        List<ProductCategory> primaryProductCategories = productCategoryService.getAllTopLevelCategories();
        model.addAttribute("primaryProductCategories", primaryProductCategories);

        return "seller/products/sellerProductRegistration";
    }

    // 상품 수정 처리
    @PostMapping("/update")
    public String updateProduct(
            @ModelAttribute ProductRegistrationRequest productUpdateRequest,
            @RequestParam(value = "thumbnailImage", required = false) List<MultipartFile> thumbnailImage,
            @RequestParam(value = "mainImage", required = false) List<MultipartFile> mainImage,
            @RequestParam(value = "detailImage", required = false) List<MultipartFile> detailImage,
            RedirectAttributes redirectAttributes) {
        try {
            productUpdateRequest.setThumbnailImage(thumbnailImage);
            productUpdateRequest.setMainImage(mainImage);
            productUpdateRequest.setDetailImage(detailImage);
            productUpdateRequest.calculateFinalPrice();
            
            productsService.updateProduct(productUpdateRequest);
            
            redirectAttributes.addFlashAttribute("message", "상품이 성공적으로 수정되었습니다.");
            return "redirect:/seller/products/list";

        } catch (Exception e) {
            System.err.println("상품 수정 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "상품 수정에 실패했습니다: " + e.getMessage());
            return "redirect:/seller/products/update/" + productUpdateRequest.getGdsNo();
        }
    }

    // 판매자 상품 목록
    @GetMapping("/list")
    public String myProductList(Model model) {
        String selUserNoToFilter = "user_no_150"; 
        String storeIdToFilter = "store_1";       
        
        List<Products> productList = productsService.getProductsBySellerAndStore(selUserNoToFilter, storeIdToFilter);

        model.addAttribute("productList", productList);
        return "seller/products/sellerProductList";
    }
    
    // 상품 삭제(비활성) 처리
    @PostMapping("/deactivate")
    public String deactivateProduct(@RequestParam("gdsNo") String gdsNo, RedirectAttributes redirectAttributes) {
        try {
            String selUserNo = "user_no_150"; 
            productsService.deactivateProduct(gdsNo, selUserNo);
            redirectAttributes.addFlashAttribute("message", "상품이 삭제되었습니다.");
        } catch (Exception e) {
            System.err.println("상품 삭제 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "상품 삭제에 실패했습니다.");
        }
        return "redirect:/seller/products/list";
    }

    // 2차 카테고리 비동기 조회
    @GetMapping("/subcategories/{primaryCategoryId}")
    @ResponseBody
    public List<ProductCategory> getSubcategories(@PathVariable("primaryCategoryId") String primaryCategoryId) {
        return productCategoryService.getSubCategoriesByParentId(primaryCategoryId);
    }

    // 상품 등록 취소
    @GetMapping("/cancelRegistration")
    public String cancelProductRegistration(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "상품 등록이 취소되었습니다.");
        return "redirect:/seller/products/list";
    }
}