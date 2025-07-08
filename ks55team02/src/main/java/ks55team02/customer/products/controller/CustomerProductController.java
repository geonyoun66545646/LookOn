package ks55team02.customer.products.controller;

import ks55team02.seller.products.domain.*;
import ks55team02.seller.products.service.ProductCategoryService;
import ks55team02.seller.products.service.ProductsService;
import ks55team02.seller.products.service.ProductSearchService;

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
    private final ProductSearchService productSearchService;

    // ColorOption DTO 및 COLOR_STYLE_MAP은 그대로 유지
    public static class ColorOption {
        private String name; private String style;
        public ColorOption(String name, String style) { this.name = name; this.style = style; }
        public String getName() { return name; } public String getStyle() { return style; }
    }
    private static final Map<String, String> COLOR_STYLE_MAP = new HashMap<>() {{
        put("블랙", "background-color: #000;"); put("화이트", "background-color: #fff; border: 1px solid #e0e0e0;");
        put("레드", "background-color: #ff0000;"); put("블루", "background-color: #0000ff;");
        put("그린", "background-color: #008000;"); put("옐로우", "background-color: #ffff00;");
        put("핑크", "background-color: #ffc0cb;"); put("그레이", "background-color: #808080;");
        put("네이비", "background-color: #000080;"); put("브라운", "background-color: #a52a2a;");
        put("베이지", "background-color: #f5f5dc;"); put("오렌지", "background-color: #ffa500;");
        put("퍼플", "background-color: #800080;"); put("실버", "background-color: #c0c0c0;");
        put("기타 패턴", "background-image: repeating-linear-gradient(45deg, #ccc 0, #ccc 5px, #eee 5px, #eee 10px);");
        put("기타 색상", "background-color: #eee; border: 1px dashed #999;"); put("default", "background-color: #ccc;");
    }};

    @GetMapping(value = {"/customer", "/customer/"})
    public String customerHomeView() {
        return "/customer/main";
    }

    @GetMapping("/customer/products")
    public String productMainView(Model model, @RequestParam(name = "category", required = false) String category) {
        String title = "전체 상품 목록";
        String breadCrumbTitle = "상품";
        List<Products> productList;

        if (category != null && !category.isEmpty()) {
            switch (category) {
                case "best": title = "베스트 상품"; breadCrumbTitle = "베스트"; break;
                case "sale": title = "세일 상품"; breadCrumbTitle = "세일"; break;
                case "new-products": title = "신상 상품"; breadCrumbTitle = "신상"; break;
            }
        }
        productList = productSearchService.getAllActiveProductsForCustomer();
        
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
            @PathVariable(value = "categoryId", required = false) String pathCategoryId) {

        if (pathCategoryId != null && !pathCategoryId.isEmpty()) {
            categoryId = pathCategoryId;
        }

        String title = "상품 목록";
        String breadCrumbTitle = "상품";

        Map<String, Object> filterParams = new HashMap<>();
        filterParams.put("gender", gender); filterParams.put("minPrice", minPrice); filterParams.put("maxPrice", maxPrice);
        filterParams.put("priceRange", priceRange); filterParams.put("colors", colors); filterParams.put("sizes", sizes);
        filterParams.put("brands", brands); filterParams.put("styles", styles); filterParams.put("discountRates", discountRates);
        filterParams.put("includeSoldOut", includeSoldOut);

        ProductCategory currentCategory = null;
        String parentCategoryName = "전체";
        String parentCategoryId = null;
        List<ProductCategory> subCategories = new ArrayList<>();

        if (categoryId != null && !categoryId.isEmpty()) {
            Map<String, Object> categoryHierarchyData = productCategoryService.getCategoryHierarchy(categoryId);
            if (categoryHierarchyData != null) {
                currentCategory = (ProductCategory) categoryHierarchyData.get("currentCategory");
                parentCategoryName = (String) categoryHierarchyData.get("parentCategoryName");
                parentCategoryId = (String) categoryHierarchyData.get("parentCategoryId");
                subCategories = (List<ProductCategory>) categoryHierarchyData.get("subCategories");
                if (currentCategory != null) {
                    title = currentCategory.getCategoryName();
                    breadCrumbTitle = currentCategory.getCategoryName();
                }
            }
        } else {
            String defaultTopLevelCategoryIdForToolbar = productCategoryService.getTopLevelCategoryIdByName("상의");
            if (defaultTopLevelCategoryIdForToolbar != null) {
                Map<String, Object> defaultCategoryData = productCategoryService.getCategoryHierarchy(defaultTopLevelCategoryIdForToolbar);
                parentCategoryName = (String) defaultCategoryData.get("parentCategoryName");
                parentCategoryId = (String) defaultCategoryData.get("parentCategoryId");
                subCategories = (List<ProductCategory>) defaultCategoryData.get("subCategories");
            } else {
                parentCategoryName = "전체 상품";
                subCategories = productCategoryService.getAllProductCategories();
            }
        }
        
        Map<String, Object> productsResult = productSearchService.getFilteredAndSortedProducts(categoryId, sortBy, filterParams, currentPage);
        List<Products> productList = (List<Products>) productsResult.get("productsList");
        long totalProductCount = (long) productsResult.get("totalProductsCount");
        int lastPage = (int) productsResult.get("lastPage");

        model.addAttribute("breadCrumbExists", true); model.addAttribute("breadCrumbTitle", breadCrumbTitle);
        model.addAttribute("title", title); model.addAttribute("productList", productList);
        model.addAttribute("totalProductCount", totalProductCount); model.addAttribute("currentPage", currentPage);
        model.addAttribute("lastPage", lastPage);
        
        model.addAttribute("parentCategoryName", parentCategoryName); model.addAttribute("parentCategoryId", parentCategoryId);
        model.addAttribute("subCategories", subCategories); model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("currentGender", gender); model.addAttribute("currentSortBy", sortBy);
        model.addAttribute("selectedColors", colors != null ? colors : new ArrayList<>());
        model.addAttribute("selectedSizes", sizes != null ? sizes : new ArrayList<>());
        model.addAttribute("selectedBrands", brands != null ? brands : new ArrayList<>());
        model.addAttribute("selectedStyles", styles != null ? styles : new ArrayList<>());
        model.addAttribute("selectedDiscountRates", discountRates != null ? discountRates : new ArrayList<>());
        model.addAttribute("selectedMinPrice", minPrice); model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("selectedPriceRange", priceRange); model.addAttribute("selectedIncludeSoldOut", includeSoldOut);

        List<ProductOptionValue> rawColorOptionValues = productsService.getAllProductColors();
        List<ColorOption> allColorOptions = new ArrayList<>();
        if (rawColorOptionValues != null) {
            for (ProductOptionValue pov : rawColorOptionValues) {
                allColorOptions.add(new ColorOption(pov.getVlNm(), COLOR_STYLE_MAP.getOrDefault(pov.getVlNm(), COLOR_STYLE_MAP.get("default"))));
            }
        }
        model.addAttribute("allColorOptions", allColorOptions);
        model.addAttribute("allApparelSizes", productsService.getAllApparelSizes());
        model.addAttribute("allShoeSizes", productsService.getAllShoeSizes());
        model.addAttribute("allBrands", productsService.getAllBrands());

        return "customer/productMain";
    }

    // ⭐⭐⭐ 이 부분이 수정되었습니다. ⭐⭐⭐
    @GetMapping("/products/categories/primary")
    @ResponseBody
    public List<ProductCategory> getPrimaryCategories() {
        System.out.println("1차 카테고리 요청 수신");
        // 변경된 서비스 메소드 이름으로 호출
        return productCategoryService.getAllTopLevelCategories();
    }

    @GetMapping("/products/categories/sub/{primaryCategoryId}")
    @ResponseBody
    public List<ProductCategory> getSubcategoriesForCustomer(@PathVariable("primaryCategoryId") String primaryCategoryId) {
        System.out.println("2차 카테고리 요청 수신: primaryCategoryId = " + primaryCategoryId);
        return productCategoryService.getSubCategoriesByParentId(primaryCategoryId);
    }

    @GetMapping("/products/detail/{productCode}")
    public String getProductDetailForCustomer(@PathVariable String productCode, Model model) {
        Products product = productsService.getProductDetailWithImages(productCode);

        if (product == null || !Boolean.TRUE.equals(product.getExpsrYn()) || !Boolean.TRUE.equals(product.getActvtnYn())) {
            return "redirect:/error/404";
        }
        model.addAttribute("product", product);

        List<ProductImage> allImages = product.getProductImages();
        ProductImage thumbnailImage = allImages.stream().filter(img -> img.getImgType() == ProductImageType.THUMBNAIL).findFirst().orElse(allImages.stream().filter(img -> img.getImgType() == ProductImageType.MAIN).findFirst().orElse(null));
        List<ProductImage> mainGalleryImages = allImages.stream().filter(img -> img.getImgType() == ProductImageType.MAIN).sorted(Comparator.comparing(ProductImage::getImgIndctSn)).collect(Collectors.toList());
        List<ProductImage> detailImages = allImages.stream().filter(img -> img.getImgType() == ProductImageType.DETAIL).sorted(Comparator.comparing(ProductImage::getImgIndctSn)).collect(Collectors.toList());
        
        model.addAttribute("thumbnailImage", thumbnailImage);
        model.addAttribute("mainGalleryImages", mainGalleryImages);
        model.addAttribute("detailImages", detailImages);

        return "customer/productDetail";
    }
}