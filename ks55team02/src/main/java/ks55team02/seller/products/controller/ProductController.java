package ks55team02.seller.products.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.common.domain.store.ProductReview;
import ks55team02.customer.login.domain.LoginUser; // LoginUser import
import ks55team02.customer.store.service.ReviewService;
import ks55team02.seller.common.service.StoreMngService;
import ks55team02.seller.products.domain.ProductCategory;
import ks55team02.seller.products.domain.ProductImage;
import ks55team02.seller.products.domain.ProductImageType;
import ks55team02.seller.products.domain.ProductRegistrationRequest;
import ks55team02.seller.products.domain.Products;
import ks55team02.seller.products.service.ProductCategoryService;
import ks55team02.seller.products.service.ProductSearchService;
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
	// ⭐⭐ 아래 두 필드를 새로 추가합니다. ⭐⭐
    private final ReviewService reviewService;
    private final ProductSearchService productSearchService;

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
			RedirectAttributes redirectAttributes, HttpSession session) {
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
		if (product == null) {
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
		if (categoryInfo != null) {
			if (categoryInfo.getCategoryLevel() == 2) {
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
	public String updateProduct(@ModelAttribute ProductRegistrationRequest productUpdateRequest,
			@RequestParam(value = "deletedImageIds", required = false) List<String> deletedImageIds,
			@RequestParam(value = "thumbnailImage", required = false) List<MultipartFile> thumbnailImage,
			@RequestParam(value = "mainImage", required = false) List<MultipartFile> mainImage,
			@RequestParam(value = "detailImage", required = false) List<MultipartFile> detailImage,
			RedirectAttributes redirectAttributes, HttpSession session) {

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
	public String myProductList(Model model, HttpSession session, RedirectAttributes redirectAttributes,
			@ModelAttribute SearchCriteria searchCriteria) {

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

		searchCriteria.setSelUserNo(selUserNo);
		searchCriteria.setStoreId(storeId);

		// ⭐ 이 부분을 추가합니다. ⭐
		// 템플릿 렌더링 오류 방지를 위해 filterConditions가 null이면 빈 리스트로 초기화
		if (searchCriteria.getFilterConditions() == null) {
			searchCriteria.setFilterConditions(new ArrayList<>());
		}

		Map<String, Object> resultMap = productsService.getProductsBySellerAndStore(searchCriteria);

		model.addAttribute("productList", resultMap.get("productList"));
		model.addAttribute("pagination", resultMap.get("pagination"));
		model.addAttribute("searchCriteria", searchCriteria);
		model.addAttribute("title", "상품 목록 조회");

		return "seller/products/sellerProductList";
	}

	// =============================================================
	// ⭐ [추가] 판매자 상품 미리보기
	// =============================================================
	// ProductController.java 파일

    // =============================================================
    // ⭐ [수정] 판매자 상품 미리보기 (세션 체크 제거)
    // =============================================================
    @GetMapping("/preview/{gdsNo}")
    public String previewProduct(@PathVariable("gdsNo") String gdsNo, Model model) {
        
        // gdsNo만으로 상품 상세 정보 조회
        Products product = productsService.getProductDetailWithImages(gdsNo);

        // 상품이 존재하지 않을 경우 에러 페이지로 이동
        if (product == null) {
            // 여기에 적절한 에러 처리 페이지 경로를 넣으세요.
            // 예: return "error/404"; 또는 return "common/error/not_found";
            return "common/error/auth_error"; 
        }
        
        // 이미지를 타입별로 필터링하는 로직은 그대로 유지
        List<ProductImage> allImages = product.getProductImages() != null ? product.getProductImages() : new ArrayList<>();
        
        ProductImage thumbnailImage = allImages.stream()
                .filter(img -> img.getImgType() == ProductImageType.THUMBNAIL)
                .findFirst()
                .orElse(null);

        List<ProductImage> mainGalleryImages = allImages.stream()
                .filter(img -> img.getImgType() == ProductImageType.MAIN)
                .sorted(Comparator.comparing(ProductImage::getImgIndctSn)) // 순서대로 정렬
                .collect(Collectors.toList());

        List<ProductImage> detailImages = allImages.stream()
                .filter(img -> img.getImgType() == ProductImageType.DETAIL)
                .sorted(Comparator.comparing(ProductImage::getImgIndctSn)) // 순서대로 정렬
                .collect(Collectors.toList());
                
        model.addAttribute("thumbnailImage", thumbnailImage);
        model.addAttribute("mainGalleryImages", mainGalleryImages);
        model.addAttribute("detailImages", detailImages);
        
        model.addAttribute("product", product);
        model.addAttribute("isPreview", true);
        model.addAttribute("title", "상품 미리보기"); // 페이지 제목 추가

        return "seller/products/sellerProductPreview"; 
    }

	// 상품 삭제(비활성) 처리
	@PostMapping("/deactivate")
	public String deactivateProduct(@RequestParam("gdsNo") String gdsNo, RedirectAttributes redirectAttributes,
			HttpSession session) {
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