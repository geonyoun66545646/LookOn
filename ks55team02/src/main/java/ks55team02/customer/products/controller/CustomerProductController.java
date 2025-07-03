package ks55team02.customer.products.controller;

import ks55team02.seller.products.domain.Products;
import ks55team02.seller.products.service.ProductsService;
import ks55team02.seller.products.domain.ProductCategory;
import ks55team02.seller.products.service.ProductCategoryService;
import ks55team02.seller.products.domain.ProductImage;
import ks55team02.seller.products.domain.ProductImageType;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;


@Controller
@RequiredArgsConstructor
public class CustomerProductController {
	
	private final ProductCategoryService productCategoryService;
    private final ProductsService productsService;
    
    // 고객 메인 페이지 뷰를 반환합니다.
    @GetMapping(value = {"/customer", "/customer/"})
    public String customerHomeView() {
        System.out.println(">>> customerHomeView 컨트롤러 메서드 진입! <<<");
        return "/customer/main";
    }

    // 기존 상품 메인 뷰를 반환합니다 (필터/정렬 기능 제외).
    @GetMapping("/customer/products")
    public String productMainView(Model model,
                                  @RequestParam(name = "category", required = false) String category) {
        System.out.println(">>> productMainView 컨트롤러 메서드 진입! (기본 상품 목록) <<<");
        String title = "전체 상품 목록";
        String breadCrumbTitle = "상품";

        List<Products> productList;

        if (category != null && !category.isEmpty()) {
            System.out.println("  > 요청된 카테고리: " + category);
            switch (category) {
                case "best":
                    title = "베스트 상품";
                    breadCrumbTitle = "베스트";
                    productList = productsService.getAllActiveProductsForCustomer();
                    break;
                case "sale":
                    title = "세일 상품";
                    breadCrumbTitle = "세일";
                    productList = productsService.getAllActiveProductsForCustomer();
                    break;
                case "new-products":
                    title = "신상 상품";
                    breadCrumbTitle = "신상";
                    productList = productsService.getAllActiveProductsForCustomer();
                    break;
                default:
                    productList = productsService.getAllActiveProductsForCustomer();
                    break;
            }
        } else {
            System.out.println("  > 카테고리 파라미터 없음. 전체 활성 상품 조회.");
            productList = productsService.getAllActiveProductsForCustomer();
        }
        
        System.out.println(">>> productMainView: productList 서비스 조회 결과 <<<");
        if (productList == null) {
            System.out.println("  > CRITICAL: productList 자체가 서비스에서 null로 반환되었습니다! 빈 리스트로 초기화합니다.");
            productList = new ArrayList<>(); // null 방어
        } else {
            System.out.println("  > productList 크기: " + productList.size());
            if (productList.isEmpty()) {
                System.out.println("  > productList가 비어 있습니다 (상품 없음).");
            } else {
                for (int i = 0; i < productList.size(); i++) {
                    Products product = productList.get(i);
                    if (product == null) {
                        System.err.println("  > CRITICAL ERROR: productList[" + i + "] 번째 요소가 null 입니다! 서비스 레이어 또는 매퍼 확인 필요.");
                    } else {
                        System.out.println("  > productList[" + i + "] - gdsNo: " + product.getGdsNo() +
                                           ", gdsNm: " + product.getGdsNm());
                        if (product.getProductCategory() == null) {
                            System.err.println("    > productList[" + i + "] - productCategory가 null입니다!");
                        } else {
                            System.out.println("    > productList[" + i + "] - categoryName: " + product.getProductCategory().getCategoryName());
                        }
                        System.out.println("    > productList[" + i + "] - thumbnailImagePath: " + product.getThumbnailImagePath());
                    }
                }
            }
        }
        System.out.println(">>> productMainView: productList 조회 결과 끝 <<<");

        model.addAttribute("breadCrumbExists", true);
        model.addAttribute("breadCrumbTitle", breadCrumbTitle);
        model.addAttribute("title", title);
        model.addAttribute("productList", productList); 

        return "customer/productMain";
    }

    // 고객용 상품 목록 뷰를 반환하며, 동적 카테고리, 필터 및 정렬 기능을 지원합니다.
    // 이 메서드가 이제 모든 상품 목록 조회(전체, 카테고리별, 필터링, 정렬)를 담당합니다.
    @GetMapping({"/products", "/products/category/{categoryId}", "/customer/products/list", "/products/list"})
    public String getFilteredAndSortedProductsView(
            Model model,
            @RequestParam(name = "categoryId", required = false) String categoryId,
            @RequestParam(name = "gender", required = false) String gender,
            @RequestParam(name = "sortBy", required = false, defaultValue = "new") String sortBy,
            @RequestParam(name = "minPrice", required = false) Integer minPrice,
            @RequestParam(name = "maxPrice", required = false) Integer maxPrice,
            @RequestParam(name = "priceRange", required = false) String priceRange,
            @RequestParam(name = "color", required = false) List<String> colors,
            @RequestParam(name = "size", required = false) List<String> sizes,
            @RequestParam(name = "brand", required = false) List<String> brands,
            @RequestParam(name = "style", required = false) List<String> styles,
            @RequestParam(name = "discountRate", required = false) List<String> discountRates,
            @RequestParam(name = "includeSoldOut", required = false, defaultValue = "false") boolean includeSoldOut,
            @RequestParam(name = "currentPage", defaultValue = "1") int currentPage, // ⭐ 추가: currentPage 파라미터
            @PathVariable(value = "categoryId", required = false) String pathCategoryId
    ) {
        System.out.println(">>> getFilteredAndSortedProductsView 컨트롤러 메서드 진입! <<<");

        if (pathCategoryId != null && !pathCategoryId.isEmpty()) {
            categoryId = pathCategoryId;
            System.out.println("  > @PathVariable로부터 categoryId: " + categoryId + " 설정됨.");
        }
        System.out.println("  > 최종 categoryId: " + categoryId);
        System.out.println("  > sortBy: " + sortBy);
        System.out.println("  > gender: " + gender);
        System.out.println("  > minPrice: " + minPrice + ", maxPrice: " + maxPrice + ", priceRange: " + priceRange);
        System.out.println("  > colors: " + (colors != null ? String.join(", ", colors) : "없음"));
        System.out.println("  > sizes: " + (sizes != null ? String.join(", ", sizes) : "없음"));
        System.out.println("  > brands: " + (brands != null ? String.join(", ", brands) : "없음"));
        System.out.println("  > styles: " + (styles != null ? String.join(", ", styles) : "없음"));
        System.out.println("  > discountRates: " + (discountRates != null ? String.join(", ", discountRates) : "없음"));
        System.out.println("  > includeSoldOut: " + includeSoldOut);
        System.out.println("  > currentPage: " + currentPage); // ⭐ 로그 추가

        String title = "상품 목록";
        String breadCrumbTitle = "상품";

        Map<String, Object> filterParams = new HashMap<>();
        filterParams.put("gender", gender);
        filterParams.put("minPrice", minPrice);
        filterParams.put("maxPrice", maxPrice);
        filterParams.put("priceRange", priceRange);
        filterParams.put("colors", colors != null ? colors : new ArrayList<>());
        filterParams.put("sizes", sizes != null ? sizes : new ArrayList<>());
        filterParams.put("brands", brands != null ? brands : new ArrayList<>());
        filterParams.put("styles", styles != null ? styles : new ArrayList<>());
        filterParams.put("discountRates", discountRates != null ? discountRates : new ArrayList<>());
        filterParams.put("includeSoldOut", includeSoldOut);


        ProductCategory currentCategory = null;
        String parentCategoryName = "전체";
        String parentCategoryId = null;
        List<ProductCategory> subCategories = null;

        System.out.println("  > 카테고리 계층 정보 조회 시작...");
        if (categoryId != null && !categoryId.isEmpty()) {
            Map<String, Object> categoryHierarchyData = productsService.getCategoryHierarchy(categoryId);
            if (categoryHierarchyData != null) {
                currentCategory = (ProductCategory) categoryHierarchyData.get("currentCategory");
                parentCategoryName = (String) categoryHierarchyData.get("parentCategoryName");
                parentCategoryId = (String) categoryHierarchyData.get("parentCategoryId");

                @SuppressWarnings("unchecked")
                List<ProductCategory> tempSubCategories = (List<ProductCategory>) categoryHierarchyData.get("subCategories");
                subCategories = tempSubCategories != null ? tempSubCategories : new ArrayList<>();

                System.out.println("    > currentCategory: " + (currentCategory != null ? currentCategory.getCategoryName() : "null"));
                System.out.println("    > parentCategoryName: " + parentCategoryName);
                System.out.println("    > subCategories.size(): " + subCategories.size());

                if (currentCategory != null) {
                    title = currentCategory.getCategoryName();
                    breadCrumbTitle = currentCategory.getCategoryName();
                }
            } else {
                System.out.println("    > productsService.getCategoryHierarchy(" + categoryId + ")가 null을 반환했습니다.");
            }
        } else {
            System.out.println("  > categoryId가 null 또는 비어있음. 기본 최상위 카테고리 조회 시도.");
            String defaultTopLevelCategoryIdForToolbar = productsService.getTopLevelCategoryIdByName("상의");
            if (defaultTopLevelCategoryIdForToolbar != null) {
                Map<String, Object> defaultCategoryData = productsService.getCategoryHierarchy(defaultTopLevelCategoryIdForToolbar);
                if (defaultCategoryData != null) {
                    parentCategoryName = (String) defaultCategoryData.get("parentCategoryName");
                    parentCategoryId = (String) defaultCategoryData.get("parentCategoryId");
                    @SuppressWarnings("unchecked")
                    List<ProductCategory> tempSubCategories = (List<ProductCategory>) defaultCategoryData.get("subCategories");
                    subCategories = tempSubCategories != null ? tempSubCategories : new ArrayList<>();
                    System.out.println("    > 기본 최상위 카테고리 (" + defaultTopLevelCategoryIdForToolbar + ") 하위 카테고리 조회 성공. 개수: " + subCategories.size());
                } else {
                    System.out.println("    > productsService.getCategoryHierarchy(" + defaultTopLevelCategoryIdForToolbar + ")가 null을 반환했습니다.");
                    subCategories = new ArrayList<>();
                }
            } else {
                System.out.println("    > 기본 최상위 카테고리 ID를 찾을 수 없음. 모든 상품 카테고리 조회 시도.");
                parentCategoryName = "전체 상품";
                subCategories = productsService.getAllProductCategories();
                if (subCategories == null) {
                    System.out.println("    > productsService.getAllProductCategories()가 null을 반환했습니다. 빈 리스트로 초기화합니다.");
                    subCategories = new ArrayList<>();
                } else {
                    System.out.println("    > 전체 상품 카테고리 조회 성공. 개수: " + subCategories.size());
                }
            }
        }
        System.out.println("  > 카테고리 계층 정보 조회 끝.");


        System.out.println("  > 필터링된 상품 목록 조회 시작...");
        // ⭐ 핵심 수정: currentPage 파라미터를 추가했습니다.
        Map<String, Object> productsResult = productsService.getFilteredAndSortedProducts(categoryId, sortBy, filterParams, currentPage);

        // ⭐ productsResult 맵에서 실제 상품 목록과 페이지네이션 정보를 추출합니다.
        List<Products> productList = (List<Products>) productsResult.get("productsList");
        long totalProductCount = (long) productsResult.get("totalProductsCount");
        int lastPage = (int) productsResult.get("lastPage");
        // currentPage는 이미 컨트롤러 파라미터로 받아서 사용하고 있으므로 다시 추출할 필요 없습니다.

        if (productList == null) {
            productList = new ArrayList<>();
            System.out.println("  > DEBUG: productsService.getFilteredAndSortedProducts()가 null을 반환했습니다. 빈 리스트로 초기화합니다.");
        }

        System.out.println(">>> getFilteredAndSortedProductsView: productList 서비스 조회 결과 <<<");
        if (productList.isEmpty()) {
            System.out.println("  > productList가 비어 있습니다 (상품 없음).");
        } else {
            for (int i = 0; i < productList.size(); i++) {
                Products product = productList.get(i);
                if (product == null) {
                    System.err.println("  > CRITICAL ERROR: productList[" + i + "] 번째 요소가 null 입니다! 서비스 레이어 확인 필요.");
                } else {
                    System.out.println("  > productList[" + i + "] - gdsNo: " + product.getGdsNo() +
                                       ", gdsNm: " + product.getGdsNm() +
                                       ", storeName: " + product.getStoreName() +
                                       ", totalStockQuantity: " + product.getTotalStockQuantity() +
                                       ", isSoldOut: " + product.getIsSoldOut());
                    if (product.getProductCategory() == null) {
                        System.err.println("    > productList[" + i + "] - productCategory가 null입니다!");
                    } else {
                        System.out.println("    > productList[" + i + "] - categoryName: " + product.getProductCategory().getCategoryName());
                    }
                    System.out.println("    > productList[" + i + "] - thumbnailImagePath: " + product.getThumbnailImagePath());
                }
            }
        }
        System.out.println(">>> getFilteredAndSortedProductsView: productList 조회 결과 끝 <<<");

        System.out.println("  > 총 필터링된 상품 개수: " + totalProductCount);


        // 뷰에 전달할 데이터 모델에 추가
        model.addAttribute("breadCrumbExists", true);
        model.addAttribute("breadCrumbTitle", breadCrumbTitle);
        model.addAttribute("title", title);
        model.addAttribute("productList", productList);
        model.addAttribute("totalProductCount", totalProductCount);
        model.addAttribute("currentPage", currentPage); // 현재 페이지 정보
        model.addAttribute("lastPage", lastPage);       // 마지막 페이지 정보


        // HTML 툴바 및 필터 모달에 필요한 데이터 추가
        model.addAttribute("parentCategoryName", parentCategoryName);
        model.addAttribute("parentCategoryId", parentCategoryId);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("currentGender", gender);
        model.addAttribute("currentSortBy", sortBy);

        // 필터 모달에 필요한 동적 옵션 데이터 추가 (null 방어)
        model.addAttribute("allColors", productsService.getAllProductColors() != null ? productsService.getAllProductColors() : new ArrayList<>());
        model.addAttribute("allApparelSizes", productsService.getAllApparelSizes() != null ? productsService.getAllApparelSizes() : new ArrayList<>());
        model.addAttribute("allShoeSizes", productsService.getAllShoeSizes() != null ? productsService.getAllShoeSizes() : new ArrayList<>());
        model.addAttribute("allBrands", productsService.getAllBrands() != null ? productsService.getAllBrands() : new ArrayList<>());

        // 현재 적용된 필터 값을 모델에 넣어 HTML에서 선택 상태를 유지
        model.addAttribute("selectedColors", colors);
        model.addAttribute("selectedSizes", sizes);
        model.addAttribute("selectedBrands", brands);
        model.addAttribute("selectedStyles", styles);
        model.addAttribute("selectedDiscountRates", discountRates);
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("selectedPriceRange", priceRange);
        model.addAttribute("selectedIncludeSoldOut", includeSoldOut);

        System.out.println(">>> getFilteredAndSortedProductsView: 모델에 데이터 추가 완료. 템플릿 반환: customer/productMain <<<");
        return "customer/productMain";
    }

    // 1차(메인) 카테고리 목록을 조회 (로그인 불필요)
    @GetMapping("/products/categories/primary")
    @ResponseBody
    public List<ProductCategory> getPrimaryCategories() {
        System.out.println("1차 카테고리 요청 수신");
        return productCategoryService.getPrimaryProductCategories();
    }

    // 특정 부모 ID에 해당하는 2차(서브) 카테고리 목록을 조회 (로그인 불필요)
    @GetMapping("/products/categories/sub/{primaryCategoryId}")
    @ResponseBody
    public List<ProductCategory> getSubcategoriesForCustomer(@PathVariable("primaryCategoryId") String primaryCategoryId) {
        System.out.println("2차 카테고리 요청 수신: primaryCategoryId = " + primaryCategoryId);
        return productCategoryService.getProductCategoriesByParentId(primaryCategoryId);
    }
    
    // 고객용 상품 상세 페이지 조회 (로그인 불필요)
    @GetMapping("/products/detail/{productCode}")
    public String getProductDetailForCustomer(@PathVariable String productCode, Model model) {
        Products product = productsService.getProductDetailWithImages(productCode);

        if (product == null || !Boolean.TRUE.equals(product.getExpsrYn()) || !Boolean.TRUE.equals(product.getActvtnYn())) {
            return "redirect:/error/404"; // 상품이 없거나 노출/활성화되지 않았다면 404 페이지로 리다이렉트
        }

        model.addAttribute("product", product);

        List<ProductImage> allImages = product.getProductImages();

        // 썸네일 이미지 (우선 썸네일 타입, 없으면 메인 타입 중 첫 번째)
        ProductImage thumbnailImage = allImages.stream()
                .filter(img -> img.getImgType() == ProductImageType.THUMBNAIL)
                .findFirst()
                .orElse(allImages.stream()
                        .filter(img -> img.getImgType() == ProductImageType.MAIN)
                        .findFirst()
                        .orElse(null)); // 모든 이미지 없을 경우 null

        // 메인 갤러리 이미지 (MAIN 타입, 순서대로 정렬)
        List<ProductImage> mainGalleryImages = allImages.stream()
                .filter(img -> img.getImgType() == ProductImageType.MAIN)
                .sorted(Comparator.comparing(ProductImage::getImgIndctSn))
                .collect(Collectors.toList());

        // 상세 이미지 (DETAIL 타입, 순서대로 정렬)
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