package ks55team02.customer.products.controller; // 패키지 경로를 customer로 변경하거나 적절히 조정

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // @RequestParam 임포트 추가
import org.springframework.web.bind.annotation.ResponseBody;

import ks55team02.seller.products.service.ProductCategoryService; // 이 줄이 있는지 확인!
import ks55team02.seller.products.domain.ProductCategory;
import ks55team02.seller.products.domain.ProductImage; // ProductImage 도메인 임포트 (seller 패키지에서 가져옴)
import ks55team02.seller.products.domain.ProductImageType; // ProductImageType enum 임포트 (seller 패키지에서 가져옴)
import ks55team02.seller.products.domain.Products; // Products 도메인 임포트 (seller 패키지에서 가져옴)
import ks55team02.seller.products.service.ProductsService; // seller 패키지에 있는 ProductsService를 사용
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/products") // 고객용 상품 관련 공통 경로 (로그인 불필요)
@RequiredArgsConstructor
public class CustomerProductController {
	
	private final ProductCategoryService productCategoryService;
    private final ProductsService productsService; // 판매자 도메인에 있는 서비스 재활용
    
    // 1차(메인) 카테고리 목록을 조회 (로그인 불필요)
    @GetMapping("/categories/primary")
    @ResponseBody
    public List<ProductCategory> getPrimaryCategories() {
        System.out.println("1차 카테고리 요청 수신"); // 디버깅용 로그
        return productCategoryService.getPrimaryProductCategories();
    }

    // 특정 부모 ID에 해당하는 2차(서브) 카테고리 목록을 조회 (로그인 불필요)
    @GetMapping("/categories/sub/{primaryCategoryId}")
    @ResponseBody
    public List<ProductCategory> getSubcategoriesForCustomer(@PathVariable("primaryCategoryId") String primaryCategoryId) {
        System.out.println("2차 카테고리 요청 수신: primaryCategoryId = " + primaryCategoryId); // 디버깅용 로그
        return productCategoryService.getProductCategoriesByParentId(primaryCategoryId);
    }
    
    // 고객용 상품 목록 조회 (로그인 불필요)
    @GetMapping("/list")
    public String getAllProductsForCustomer(Model model) {
        List<Products> productList = productsService.getAllActiveProductsForCustomer();
        model.addAttribute("productList", productList);
        return "customer/products/customerProductList"; // 고객용 상품 목록 템플릿 경로 (예: customerProductList.html)
    }

    // 고객용: 특정 카테고리에 속하는 활성화 및 노출 상품 목록을 조회합니다.
    @GetMapping("/category/{categoryId}") // @PathVariable 사용
    public String getProductsByCategoryForCustomer(@PathVariable("categoryId") String categoryId, Model model) {
        List<Products> productList = productsService.getActiveProductsForCustomerByCategory(categoryId);
        model.addAttribute("productList", productList);
        model.addAttribute("currentCategory", categoryId); // 현재 카테고리 ID를 뷰에 전달
        return "customer/products/customerProductList"; // 카테고리별 목록도 동일한 템플릿 사용 가능
    }

    // 고객용 상품 상세 페이지 조회 (로그인 불필요)
    @GetMapping("/detail/{productCode}")
    public String getProductDetailForCustomer(@PathVariable String productCode, Model model) {
        Products product = productsService.getProductDetailWithImages(productCode);

        if (product == null || !Boolean.TRUE.equals(product.getExpsrYn()) || !Boolean.TRUE.equals(product.getActvtnYn())) {
            return "redirect:/error/404"; 
        }

        model.addAttribute("product", product);

        List<ProductImage> allImages = product.getProductImages();

        ProductImage thumbnailImage = allImages.stream()
                .filter(img -> img.getImgType() == ProductImageType.THUMBNAIL)
                .findFirst()
                .orElse(allImages.stream()
                        .filter(img -> img.getImgType() == ProductImageType.MAIN)
                        .findFirst()
                        .orElse(null));

        List<ProductImage> mainGalleryImages = allImages.stream()
                .filter(img -> img.getImgType() == ProductImageType.MAIN)
                .sorted(Comparator.comparing(ProductImage::getImgIndctSn))
                .collect(Collectors.toList());

        List<ProductImage> detailImages = allImages.stream()
                .filter(img -> img.getImgType() == ProductImageType.DETAIL)
                .sorted(Comparator.comparing(ProductImage::getImgIndctSn))
                .collect(Collectors.toList());

        model.addAttribute("thumbnailImage", thumbnailImage);
        model.addAttribute("mainGalleryImages", mainGalleryImages);
        model.addAttribute("detailImages", detailImages);

        return "customer/productDetail"; 
    }
}