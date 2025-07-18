package ks55team02.seller.products.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession; // HttpSession import
import ks55team02.customer.login.domain.LoginUser; // LoginUser import
import ks55team02.seller.common.service.impl.StoreMngService;
import ks55team02.seller.products.domain.ProductCategory;
import ks55team02.seller.products.domain.ProductRegistrationRequest;
import ks55team02.seller.products.domain.Products;
import ks55team02.seller.products.service.ProductCategoryService;
import ks55team02.seller.products.service.ProductsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/seller/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
	
    private final ProductsService productsService;
    private final ProductCategoryService productCategoryService;
    private final StoreMngService storeMngService;

    // 상품 등록 폼
    @GetMapping("/registration")
    public String registerProductForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/seller/login";
        }
        
        String selUserNo = loginUser.getUserNo();
        String storeId = storeMngService.getStoreIdBySellerId(selUserNo);
        
        if (storeId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "상점 정보가 없습니다. 상점 신청을 완료해주세요.");
            return "redirect:/seller";
        }

        ProductRegistrationRequest requestDto = new ProductRegistrationRequest();
        requestDto.setSelUserNo(selUserNo);
        requestDto.setStoreId(storeId);
        
        model.addAttribute("productRegistrationRequest", requestDto);
        model.addAttribute("primaryProductCategories", productCategoryService.getAllTopLevelCategories());
        model.addAttribute("isUpdate", false);
        return "seller/products/sellerProductRegistration";
    }

    // 상품 등록 처리
    @PostMapping("/register")
    public String registerProduct(@ModelAttribute ProductRegistrationRequest productRegistrationRequest,
                                  @RequestParam(value = "thumbnailImage", required = false) List<MultipartFile> thumbnailImage,
                                  @RequestParam(value = "mainImage", required = false) List<MultipartFile> mainImage,
                                  @RequestParam(value = "detailImage", required = false) List<MultipartFile> detailImage,
                                  RedirectAttributes redirectAttributes,
                                  HttpSession session) {
        try {
            LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
            if (loginUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "세션이 만료되었거나 로그인이 필요합니다.");
                return "redirect:/seller/login";
            }

            productRegistrationRequest.setThumbnailImage(thumbnailImage);
            productRegistrationRequest.setMainImage(mainImage);
            productRegistrationRequest.setDetailImage(detailImage);
            productRegistrationRequest.calculateFinalPrice();
            
            // 하드코딩된 값 교체
            productRegistrationRequest.setStoreId(storeMngService.getStoreIdBySellerId(loginUser.getUserNo()));
            productRegistrationRequest.setSelUserNo(loginUser.getUserNo());

            productsService.addProduct(productRegistrationRequest);

            redirectAttributes.addFlashAttribute("message", "상품이 성공적으로 등록되었습니다.");
            return "redirect:/seller/products/list";

        } catch (Exception e) {
            log.error("상품 등록 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "상품 등록에 실패했습니다: " + e.getMessage());
            return "redirect:/seller/products/registration";
        }
    }

    // 상품 수정 폼
    @GetMapping("/update/{gdsNo}")
    public String updateProductForm(@PathVariable("gdsNo") String gdsNo, Model model, HttpSession session) {
        // TODO: 로그인된 판매자의 상품이 맞는지 권한 체크 로직 추가 필요
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
        model.addAttribute("primaryProductCategories", productCategoryService.getAllTopLevelCategories());
        return "seller/products/sellerProductRegistration";
    }

    // 상품 수정 처리
    @PostMapping("/update")
    public String updateProduct(
            @ModelAttribute ProductRegistrationRequest productUpdateRequest,
            @RequestParam(value = "deletedImageIds", required = false) List<String> deletedImageIds,
            @RequestParam(value = "thumbnailImage", required = false) List<MultipartFile> thumbnailImage,
            @RequestParam(value = "mainImage", required = false) List<MultipartFile> mainImage,
            @RequestParam(value = "detailImage", required = false) List<MultipartFile> detailImage,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
    	
        try {
            LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
            if (loginUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "세션이 만료되었거나 로그인이 필요합니다.");
                return "redirect:/seller/login";
            }
            
        	productUpdateRequest.setDeletedImageIds(deletedImageIds);
            productUpdateRequest.setThumbnailImage(thumbnailImage);
            productUpdateRequest.setMainImage(mainImage);
            productUpdateRequest.setDetailImage(detailImage);
            productUpdateRequest.calculateFinalPrice();
            
            // 하드코딩된 값 교체
            productUpdateRequest.setSelUserNo(loginUser.getUserNo());
            productUpdateRequest.setStoreId(storeMngService.getStoreIdBySellerId(loginUser.getUserNo()));
            
            productsService.updateProduct(productUpdateRequest);
            
            redirectAttributes.addFlashAttribute("message", "상품이 성공적으로 수정되었습니다.");
            return "redirect:/seller/products/list";

        } catch (Exception e) {
            log.error("상품 수정 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "상품 수정에 실패했습니다: " + e.getMessage());
            return "redirect:/seller/products/update/" + productUpdateRequest.getGdsNo();
        }
    }

    // 판매자 상품 목록
    @GetMapping("/list")
    public String myProductList(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/seller/login";
        }
        
        String selUserNo = loginUser.getUserNo();
        String storeId = storeMngService.getStoreIdBySellerId(selUserNo);
        
        if (storeId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "상점 정보가 없습니다. 상점 신청을 완료해주세요.");
            return "redirect:/seller";
        }
        
        List<Products> productList = productsService.getProductsBySellerAndStore(selUserNo, storeId);
        
        model.addAttribute("productList", productList);
        return "seller/products/sellerProductList";
    }
    
    // 상품 삭제(비활성) 처리
    @PostMapping("/deactivate")
    public String deactivateProduct(@RequestParam("gdsNo") String gdsNo, RedirectAttributes redirectAttributes, HttpSession session) {
        try {
            LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
            if (loginUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "세션이 만료되었거나 로그인이 필요합니다.");
                return "redirect:/seller/login";
            }
            // TODO: 해당 상품이 정말 이 판매자의 상품인지 권한 체크 로직 추가 필요
            productsService.deactivateProduct(gdsNo, loginUser.getUserNo());
            redirectAttributes.addFlashAttribute("message", "상품이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("상품 삭제 중 오류 발생", e);
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