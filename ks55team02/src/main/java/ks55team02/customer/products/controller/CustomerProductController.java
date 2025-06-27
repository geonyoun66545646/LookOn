package ks55team02.customer.products.controller; // 패키지 경로를 customer로 변경하거나 적절히 조정

import ks55team02.seller.products.domain.Products; // Products 도메인 임포트 (seller 패키지에서 가져옴)
import ks55team02.seller.products.domain.ProductImage; // ProductImage 도메인 임포트 (seller 패키지에서 가져옴)
import ks55team02.seller.products.domain.ProductImageType; // ProductImageType enum 임포트 (seller 패키지에서 가져옴)
import ks55team02.seller.products.service.ProductsService; // seller 패키지에 있는 ProductsService를 사용

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/products") // 고객용 상품 관련 공통 경로 (로그인 불필요)
@RequiredArgsConstructor
public class CustomerProductController {

    private final ProductsService productsService; // 판매자 도메인에 있는 서비스 재활용

    /**
     * 고객용 상품 목록 조회 (로그인 불필요)
     * 활성화 및 노출 상태인 상품만 조회.
     * @param model 모델 객체
     * @return 고객용 상품 목록 템플릿 경로
     */
    @GetMapping("/list")
    public String getAllProductsForCustomer(Model model) {
        // ProductsService에 'getAllActiveProductsForCustomer()' 메서드가 정의되어 있어야 합니다.
        // 이 메서드는 isExpsrYn() == true 이고 isActvtnYn() == true 인 상품만 가져와야 합니다.
        List<Products> productList = productsService.getAllActiveProductsForCustomer();
        model.addAttribute("productList", productList);
        return "customer/products/customerProductList"; // 고객용 상품 목록 템플릿 경로 (예: customerProductList.html)
    }

    /**
     * 고객용 상품 상세 페이지 조회 (로그인 불필요)
     * 상품은 노출 및 활성화 상태인 경우에만 접근 가능.
     * @param productCode 조회할 상품 번호
     * @param model 모델 객체
     * @return 상품 상세 템플릿 경로
     */
    @GetMapping("/detail/{productCode}")
    public String getProductDetailForCustomer(@PathVariable String productCode, Model model) {
        // 서비스 계층에서 상품 상세 정보와 이미지를 함께 가져오는 메서드를 호출
        Products product = productsService.getProductDetailWithImages(productCode);

        // 상품이 없거나, 노출(expsrYn) 또는 활성화(actvtnYn) 상태가 아니면 404 처리
        // ⭐ Products 도메인에 isExpsrYn() 및 isActvtnYn() 메서드가 정의되어 있어야 합니다. ⭐
        if (product == null || !Boolean.TRUE.equals(product.getExpsrYn()) || !Boolean.TRUE.equals(product.getActvtnYn())) {
            // 참고: product.getExpsrYn()이 null일 경우 false로 간주하여 접근을 막습니다.
            return "redirect:/error/404"; // 또는 적절한 에러 페이지
        }

        // 뷰로 데이터를 전달
        model.addAttribute("product", product);

        // 이미지 타입별로 정렬하거나 필터링하여 뷰로 보낼 수 있도록 분류
        List<ProductImage> allImages = product.getProductImages();

        // 썸네일 이미지 (우선순위: THUMBNAIL -> MAIN)
        ProductImage thumbnailImage = allImages.stream()
                .filter(img -> img.getImgType() == ProductImageType.THUMBNAIL)
                .findFirst()
                .orElse(allImages.stream()
                        .filter(img -> img.getImgType() == ProductImageType.MAIN)
                        .findFirst()
                        .orElse(null));

        // 메인 갤러리 이미지 (MAIN 타입의 이미지들을 imgIndctSn 순서로 정렬하여 사용)
        // ⭐ ProductImage 도메인에 getImgIndctSn() 메서드가 정의되어 있어야 합니다. ⭐
        List<ProductImage> mainGalleryImages = allImages.stream()
                .filter(img -> img.getImgType() == ProductImageType.MAIN)
                .sorted(Comparator.comparing(ProductImage::getImgIndctSn))
                .collect(Collectors.toList());

        // 상세 설명 이미지 (DETAIL 타입의 이미지들을 imgIndctSn 순서로 정렬하여 사용)
        List<ProductImage> detailImages = allImages.stream()
                .filter(img -> img.getImgType() == ProductImageType.DETAIL)
                .sorted(Comparator.comparing(ProductImage::getImgIndctSn))
                .collect(Collectors.toList());

        model.addAttribute("thumbnailImage", thumbnailImage);
        model.addAttribute("mainGalleryImages", mainGalleryImages);
        model.addAttribute("detailImages", detailImages);

        // ⭐⭐⭐ 이 부분을 수정했습니다. productDetail.html의 실제 경로에 맞춥니다. ⭐⭐⭐
        return "customer/productDetail"; // Thymeleaf 템플릿 경로를 실제 위치에 맞게 수정
    }
}