package ks55team02.customer.products.controller;

import ks55team02.seller.products.domain.Products;
import ks55team02.seller.products.service.ProductsService;
import ks55team02.seller.products.domain.ProductCategory;
import ks55team02.seller.products.service.ProductCategoryService;
import ks55team02.seller.products.domain.ProductImage;
import ks55team02.seller.products.domain.ProductImageType;
import ks55team02.seller.products.domain.ProductOptionValue;

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

    // =============== ColorOption DTO 및 COLOR_STYLE_MAP은 그대로 유지 ===============

    public static class ColorOption {
        private String name;
        private String style;

        public ColorOption(String name, String style) {
            this.name = name;
            this.style = style;
        }

        public String getName() { return name; }
        public String getStyle() { return style; }
    }

    private static final Map<String, String> COLOR_STYLE_MAP = new HashMap<>();
    static {
        COLOR_STYLE_MAP.put("블랙", "background-color: #000;");
        COLOR_STYLE_MAP.put("화이트", "background-color: #fff; border: 1px solid #e0e0e0;");
        COLOR_STYLE_MAP.put("레드", "background-color: #ff0000;");
        COLOR_STYLE_MAP.put("블루", "background-color: #0000ff;");
        COLOR_STYLE_MAP.put("그린", "background-color: #008000;");
        COLOR_STYLE_MAP.put("옐로우", "background-color: #ffff00;");
        COLOR_STYLE_MAP.put("핑크", "background-color: #ffc0cb;");
        COLOR_STYLE_MAP.put("그레이", "background-color: #808080;");
        COLOR_STYLE_MAP.put("네이비", "background-color: #000080;");
        COLOR_STYLE_MAP.put("브라운", "background-color: #a52a2a;");
        COLOR_STYLE_MAP.put("베이지", "background-color: #f5f5dc;");
        COLOR_STYLE_MAP.put("오렌지", "background-color: #ffa500;");
        COLOR_STYLE_MAP.put("퍼플", "background-color: #800080;");
        COLOR_STYLE_MAP.put("실버", "background-color: #c0c0c0;");
        COLOR_STYLE_MAP.put("기타 패턴", "background-image: repeating-linear-gradient(45deg, #ccc 0, #ccc 5px, #eee 5px, #eee 10px);");
        COLOR_STYLE_MAP.put("기타 색상", "background-color: #eee; border: 1px dashed #999;");
        COLOR_STYLE_MAP.put("default", "background-color: #ccc;");
    }

    // =============== ColorOption DTO 및 COLOR_STYLE_MAP은 그대로 유지 ===============


    @GetMapping(value = {"/customer", "/customer/"})
    public String customerHomeView() {
        System.out.println(">>> customerHomeView 컨트롤러 메서드 진입! <<<");
        return "/customer/main";
    }

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
            productList = new ArrayList<>();
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

    @GetMapping({"/products", "/products/category/{categoryId}", "/customer/products/list", "/products/list"})
    @SuppressWarnings("unchecked")
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
            @RequestParam(name = "currentPage", defaultValue = "1") int currentPage,
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
        System.out.println("  > currentPage: " + currentPage);

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
        Map<String, Object> productsResult = productsService.getFilteredAndSortedProducts(categoryId, sortBy, filterParams, currentPage);

        List<Products> productList = (List<Products>) productsResult.get("productsList");
        long totalProductCount = (long) productsResult.get("totalProductsCount");
        int lastPage = (int) productsResult.get("lastPage");

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
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("lastPage", lastPage);


        // HTML 툴바 및 필터 모달에 필요한 데이터 추가
        model.addAttribute("parentCategoryName", parentCategoryName);
        model.addAttribute("parentCategoryId", parentCategoryId);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("currentGender", gender);
        model.addAttribute("currentSortBy", sortBy);

        // ⭐⭐⭐ 이 부분이 핵심 수정입니다! ⭐⭐⭐
        // @RequestParam으로 받은 List<String> 변수들이 null일 경우, 빈 ArrayList로 초기화하여 모델에 추가합니다.
        // 이렇게 하면 HTML에서 #lists.contains()를 사용할 때 NullPointerException이 발생하지 않습니다.
        model.addAttribute("selectedColors", colors != null ? colors : new ArrayList<>());
        model.addAttribute("selectedSizes", sizes != null ? sizes : new ArrayList<>());
        model.addAttribute("selectedBrands", brands != null ? brands : new ArrayList<>());
        model.addAttribute("selectedStyles", styles != null ? styles : new ArrayList<>());
        model.addAttribute("selectedDiscountRates", discountRates != null ? discountRates : new ArrayList<>());
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("selectedPriceRange", priceRange);
        model.addAttribute("selectedIncludeSoldOut", includeSoldOut); // boolean은 기본적으로 null이 될 수 없음

        List<ProductOptionValue> rawColorOptionValues = productsService.getAllProductColors();
        List<ColorOption> allColorOptions = new ArrayList<>();
        if (rawColorOptionValues != null) {
            for (ProductOptionValue pov : rawColorOptionValues) {
                String colorName = pov.getVlNm();
                String style = COLOR_STYLE_MAP.getOrDefault(colorName, COLOR_STYLE_MAP.get("default"));
                allColorOptions.add(new ColorOption(colorName, style));
            }
        }
        model.addAttribute("allColorOptions", allColorOptions);


        model.addAttribute("allApparelSizes", productsService.getAllApparelSizes() != null ? productsService.getAllApparelSizes() : new ArrayList<>());
        model.addAttribute("allShoeSizes", productsService.getAllShoeSizes() != null ? productsService.getAllShoeSizes() : new ArrayList<>());
        // allBrands는 Store 객체 리스트여야 하므로, 여기에 Store 대신 String이 직접 들어갈 수 있다면 문제가 됩니다.
        // ProductsService의 getAllBrands()가 어떤 타입의 리스트를 반환하는지 확인 필요합니다.
        // 여기서는 일단 Store 객체 리스트로 가정하고, HTML에서 storeId를 사용하도록 수정합니다.
        model.addAttribute("allBrands", productsService.getAllBrands() != null ? productsService.getAllBrands() : new ArrayList<>());

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
            return "redirect:/error/404";
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
                        .orElse(null));

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