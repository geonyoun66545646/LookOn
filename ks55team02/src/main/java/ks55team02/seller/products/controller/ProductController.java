package ks55team02.seller.products.controller;

import ks55team02.seller.products.domain.ProductRegistrationRequest;
import ks55team02.seller.products.service.ProductsService;
import ks55team02.seller.products.domain.ProductCategory;
import ks55team02.seller.products.service.ProductCategoryService;
// ⭐⭐⭐ Products 도메인만 추가 ⭐⭐⭐
import ks55team02.seller.products.domain.Products; // 판매자 리스트에서 사용할 Products 도메인 임포트
// ⭐⭐⭐ 여기까지 추가 ⭐⭐⭐

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/seller/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductsService productsService;
    private final ProductCategoryService productCategoryService;

    // 상품 등록 폼 페이지 요청 (GET: /seller/products/registration)
    @GetMapping("/registration")
    public String registerProductForm(Model model) {
        model.addAttribute("productRegistrationRequest", new ProductRegistrationRequest());

        List<ProductCategory> primaryProductCategories = productCategoryService.getPrimaryProductCategories();
        model.addAttribute("primaryProductCategories", primaryProductCategories);

        return "seller/products/sellerProductRegistration";
    }

    // ⭐⭐ 2차 카테고리 조회 API 엔드포인트 ⭐⭐
    @GetMapping("/subcategories/{primaryCategoryId}")
    @ResponseBody // 이 어노테이션이 중요합니다. 이 메서드의 반환 값을 HTTP 응답 본문에 직접 JSON 형태로 넣어줍니다.
    public List<ProductCategory> getSubcategories(@PathVariable("primaryCategoryId") String primaryCategoryId) { // ⭐ Long -> String으로 변경 ⭐
        System.out.println("2차 카테고리 요청 수신: primaryCategoryId = " + primaryCategoryId); // 디버깅용 로그

        // 서비스 메서드 호출 이름은 이미 통일되었다고 가정합니다.
        // ProductCategoryService 인터페이스에 getProductCategoriesByParentId(String) 메서드가 있어야 합니다.
        List<ProductCategory> subcategories = productCategoryService.getProductCategoriesByParentId(primaryCategoryId);
        
        return subcategories;
    }


    // 상품 등록 처리 (POST: /seller/products/register)
    @PostMapping("/register")
    public String registerProduct(@ModelAttribute ProductRegistrationRequest productRegistrationRequest,
                                  @RequestParam(value = "thumbnailImage", required = false) List<MultipartFile> thumbnailImage,
                                  @RequestParam(value = "mainImage", required = false) List<MultipartFile> mainImage,
                                  @RequestParam(value = "detailImage", required = false) List<MultipartFile> detailImage,
                                  RedirectAttributes redirectAttributes) {
        try {
            // 이미지 파일 수 바인딩
            productRegistrationRequest.setThumbnailImage(thumbnailImage);
            productRegistrationRequest.setMainImage(mainImage);
            productRegistrationRequest.setDetailImage(detailImage);

            // ✅ 썸네일, 메인, 상세 이미지 갯수 유효성 검사 (선택 사항)
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

            // ✅ 최종 가격 계산
            productRegistrationRequest.calculateFinalPrice();

            // ✅ 임시: 하드코딩된 사용자 정보 (로그인 후 변경 필요)
            // TODO: 로그인 유저 정보로 대체할 것
            productRegistrationRequest.setStoreId("store_1");
            productRegistrationRequest.setSelUserNo("user_no_150");

            // 서비스 호출
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

    // ⭐⭐ 상품 등록 취소 처리 (GET: /seller/products/cancelRegistration) ⭐⭐
    @GetMapping("/cancelRegistration")
    public String cancelProductRegistration(RedirectAttributes redirectAttributes) {
        // 여기에 취소 시 필요한 백엔드 로직을 추가할 수 있습니다.
        // 예: 임시로 업로드된 파일 삭제, 임시 DB 데이터 삭제 등
        // 현재는 단순히 리다이렉션만 수행합니다.

        redirectAttributes.addFlashAttribute("message", "상품 등록이 취소되었습니다.");
        return "redirect:/seller/products/list"; // 상품 목록 페이지로 리다이렉션
    }

    // 상품 리스트 페이지 요청 (GET: /seller/products/list)
    @GetMapping("/list")
    public String myProductList(Model model) {
        // --- 이 부분이 변경됩니다. ---
        // ✅ 임시: 하드코딩된 판매자 ID 및 스토어 ID (로그인 정보로 대체 예정)
        // 이 두 값은 실제 등록된 상품의 sel_user_no와 store_id 값과 정확히 일치해야 합니다.
        String selUserNoToFilter = "user_no_150"; 
        String storeIdToFilter = "store_1";       
        
        // 변경된 서비스 메서드를 호출하도록 수정합니다.
        // 매퍼 XML에서 변경한 ID와 서비스 인터페이스/구현체에서 정의할 메서드 이름에 맞춰야 합니다.
        List<Products> productList = // ⭐ Products 임포트 필요 ⭐
            productsService.getProductsBySellerAndStore(selUserNoToFilter, storeIdToFilter); // 이전에 구현했던 메서드를 사용

        model.addAttribute("productList", productList);
        return "seller/products/sellerProductList"; // 상품 리스트 페이지 템플릿 이름
    }
}