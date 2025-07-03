package ks55team02.seller.products.service.impl;

// 필요한 모든 도메인/DTO 클래스 임포트
import ks55team02.seller.products.domain.*;
import ks55team02.seller.products.domain.ProductRegistrationRequest.ProductCombinationData; // 내부 클래스 명시적 임포트
import ks55team02.seller.products.mapper.ProductsMapper;
import ks55team02.seller.products.service.ProductsService; // 서비스 인터페이스 임포트
import ks55team02.seller.stores.domain.Stores;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductsServiceImpl implements ProductsService {
    private final ProductsMapper productsMapper;

    @Value("${product.upload.dir}")
    private String uploadDir;

    // 판매자 번호(selUserNo)와 스토어 ID(storeId)에 해당하는 상품 목록을 조회합니다.
    @Override
    public List<Products> getProductsBySellerAndStore(String selUserNo, String storeId) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("selUserNo", selUserNo);
        paramMap.put("storeId", storeId);
        return productsMapper.getProductsBySellerAndStore(paramMap);
    }

    // 새로운 상품을 등록하고 관련 이미지, 옵션, 옵션 조합별 재고 정보를 처리합니다.
    @Override
    @Transactional
    public void addProduct(ProductRegistrationRequest request) {
        String selUserNo = request.getSelUserNo();
        String storeId = request.getStoreId();

        // 1. 상품 번호(gdsNo) 생성
        String gdsNo = productsMapper.getMaxProductCode();
        request.setGdsNo(gdsNo);

        // 2. 기본 상품 정보 설정 및 PRODUCTS 테이블에 저장
        Products product = new Products();
        product.setGdsNo(gdsNo);
        product.setStoreId(storeId);

        String finalCategoryNo = null;
        if (StringUtils.hasText(request.getProductCategory2())) {
            finalCategoryNo = request.getProductCategory2();
        } else if (StringUtils.hasText(request.getProductCategory1())) {
            finalCategoryNo = request.getProductCategory1();
        }
        if (!StringUtils.hasText(finalCategoryNo)) {
            throw new IllegalArgumentException("상품 카테고리가 유효하지 않습니다. 1차 또는 2차 카테고리를 선택해주세요.");
        }
        product.setCtgryNo(finalCategoryNo);

        product.setSelUserNo(selUserNo);
        product.setMdfrNo(selUserNo);
        product.setGdsNm(request.getProductName());
        product.setGdsExpln(request.getProductDescription());
        product.setBasPrc(request.getBasePrice() != null ? request.getBasePrice().intValue() : 0);
        product.setDscntRt(request.getDiscountRate());

        product.setMinPurchaseQty(request.getMinPurchase());
        product.setMaxPurchaseQty(request.getMaxPurchase());
        
        request.calculateFinalPrice();
        product.setLastSelPrc(request.getFinalPrice() != null ? request.getFinalPrice().intValue() : 0);

        product.setRegDt(LocalDateTime.now());
        product.setExpsrYn(true);
        product.setActvtnYn(true);

        productsMapper.addProduct(product);

        // 3. 이미지 업로드 및 PRODUCT_IMAGE 테이블에 DB 저장
        int imgIndctSn = 1;

        if (request.getThumbnailImage() != null) {
            for (MultipartFile file : request.getThumbnailImage()) {
                saveAndInsertImage(file, gdsNo, selUserNo, productsMapper.getMaxImageNo(), imgIndctSn++, ProductImageType.THUMBNAIL);
            }
        }
        if (request.getMainImage() != null) {
            for (MultipartFile file : request.getMainImage()) {
                saveAndInsertImage(file, gdsNo, selUserNo, productsMapper.getMaxImageNo(), imgIndctSn++, ProductImageType.MAIN);
            }
        }
        if (request.getDetailImage() != null) {
            for (MultipartFile file : request.getDetailImage()) {
                saveAndInsertImage(file, gdsNo, selUserNo, productsMapper.getMaxImageNo(), imgIndctSn++, ProductImageType.DETAIL);
            }
        }

        // 4. 옵션 정보 (PRODUCT_OPTION 및 PRODUCT_OPTION_VALUE) 생성 및 저장
        Map<String, String> genderOptionValueMap = new HashMap<>();
        Map<String, String> colorOptionValueMap = new HashMap<>();
        Map<String, String> sizeOptionValueMap = new HashMap<>();

        int optOrder = 1;
        if (request.getGenderOption() != null && !request.getGenderOption().isEmpty()) {
            String genderOptNo = productsMapper.getMaxOptionNo();
            genderOptionValueMap = insertOptionAndValue(gdsNo, selUserNo, "성별", "S", optOrder++, genderOptNo, List.of(request.getGenderOption()));
        }
        if (request.getColorOptions() != null && !request.getColorOptions().isEmpty()) {
            String colorOptNo = productsMapper.getMaxOptionNo();
            colorOptionValueMap = insertOptionAndValue(gdsNo, selUserNo, "색상", "S", optOrder++, colorOptNo, request.getColorOptions());
        }
        if (request.getSizeOptions() != null && !request.getSizeOptions().isEmpty()) {
            String sizeOptNo = productsMapper.getMaxOptionNo();
            sizeOptionValueMap = insertOptionAndValue(gdsNo, selUserNo, "사이즈", "S", optOrder++, sizeOptNo, request.getSizeOptions());
        }

        // 5. 옵션 조합별 재고 수량 및 매핑 정보 저장
        if (request.getProductOptionCombinations() != null && !request.getProductOptionCombinations().isEmpty()) {
            for (ProductCombinationData combinationData : request.getProductOptionCombinations()) {
                String gdsSttsNo = productsMapper.getMaxStatusNo();

                ProductStatus productStatus = new ProductStatus();
                productStatus.setGdsSttsNo(gdsSttsNo);
                productStatus.setGdsNo(gdsNo);
                productStatus.setCreatrNo(selUserNo);
                productStatus.setMdfrNo(selUserNo);
                productStatus.setSelPsbltyQntty(combinationData.getQuantity());
                productStatus.setRegDt(LocalDateTime.now());
                productStatus.setSldoutYn(combinationData.getQuantity() <= 0);
                productStatus.setActvtnYn(true);

                productsMapper.insertProductStatus(productStatus);

                String finalOptVlNo1 = null;
                if (StringUtils.hasText(combinationData.getOptVlNo1())) {
                    finalOptVlNo1 = genderOptionValueMap.get(combinationData.getOptVlNo1());
                }

                String finalOptVlNo2 = null;
                if (StringUtils.hasText(combinationData.getOptVlNo2())) {
                    finalOptVlNo2 = colorOptionValueMap.get(combinationData.getOptVlNo2());
                }

                String finalOptVlNo3 = null;
                if (StringUtils.hasText(combinationData.getOptVlNo3())) {
                    finalOptVlNo3 = sizeOptionValueMap.get(combinationData.getOptVlNo3());
                }

                System.out.println("CombinationData optVlNo1 (from frontend, assumed name): " + combinationData.getOptVlNo1() + ", Mapped finalOptVlNo1: " + finalOptVlNo1);
                System.out.println("CombinationData optVlNo2 (from frontend, assumed name): " + combinationData.getOptVlNo2() + ", Mapped finalOptVlNo2: " + finalOptVlNo2);
                System.out.println("CombinationData optVlNo3 (from frontend, assumed name): " + combinationData.getOptVlNo3() + ", Mapped finalOptVlNo3: " + finalOptVlNo3);


                if (StringUtils.hasText(finalOptVlNo1)) {
                    StatusOptionMapping mapping1 = new StatusOptionMapping();
                    mapping1.setGdsSttsNo(gdsSttsNo);
                    mapping1.setOptVlNo(finalOptVlNo1);
                    mapping1.setCreatrNo(selUserNo);
                    mapping1.setDelUserNo(null);
                    mapping1.setCrtYmd(LocalDateTime.now());
                    mapping1.setInactvtnDt(null);
                    mapping1.setActvtnYn(true);
                    productsMapper.insertStatusOptionMapping(mapping1);
                }
                if (StringUtils.hasText(finalOptVlNo2)) {
                    StatusOptionMapping mapping2 = new StatusOptionMapping();
                    mapping2.setGdsSttsNo(gdsSttsNo);
                    mapping2.setOptVlNo(finalOptVlNo2);
                    mapping2.setCreatrNo(selUserNo);
                    mapping2.setDelUserNo(null);
                    mapping2.setCrtYmd(LocalDateTime.now());
                    mapping2.setInactvtnDt(null);
                    mapping2.setActvtnYn(true);
                    productsMapper.insertStatusOptionMapping(mapping2);
                }
                if (StringUtils.hasText(finalOptVlNo3)) {
                    StatusOptionMapping mapping3 = new StatusOptionMapping();
                    mapping3.setGdsSttsNo(gdsSttsNo);
                    mapping3.setOptVlNo(finalOptVlNo3);
                    mapping3.setCreatrNo(selUserNo);
                    mapping3.setDelUserNo(null);
                    mapping3.setCrtYmd(LocalDateTime.now());
                    mapping3.setInactvtnDt(null);
                    mapping3.setActvtnYn(true);
                    productsMapper.insertStatusOptionMapping(mapping3);
                }
            }
        } else {
            String gdsSttsNo = productsMapper.getMaxStatusNo();
            ProductStatus productStatus = new ProductStatus();
            productStatus.setGdsSttsNo(gdsSttsNo);
            productStatus.setGdsNo(gdsNo);
            productStatus.setCreatrNo(selUserNo);
            productStatus.setMdfrNo(selUserNo);
            productStatus.setSelPsbltyQntty(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
            productStatus.setRegDt(LocalDateTime.now());
            productStatus.setSldoutYn(request.getStockQuantity() != null && request.getStockQuantity() <= 0);
            productStatus.setActvtnYn(true);
            productsMapper.insertProductStatus(productStatus);
        }
    }

    private void saveAndInsertImage(MultipartFile file, String gdsNo, String creatorNo, String imgNo, int imgIndctSn, ProductImageType imageType) {
        if (file.isEmpty()) return;

        Path filePath = null;

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String ext = "";
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String savedFileName = imgNo + ext;
            filePath = uploadPath.resolve(savedFileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            ProductImage productImage = new ProductImage();
            productImage.setImgNo(imgNo);
            productImage.setGdsNo(gdsNo);
            productImage.setCreatrNo(creatorNo);
            productImage.setMdfrNo(creatorNo);
            productImage.setImgFilePathNm("/maincss/assets/images/sellerUploads/" + savedFileName);
            productImage.setImgIndctSn(imgIndctSn);
            productImage.setRegDt(LocalDateTime.now());
            productImage.setImgType(imageType);
            productImage.setActvtnYn(true);

            productsMapper.insertProductImage(productImage);

        } catch (IOException e) {
            String errorFilePath = (filePath != null) ? filePath.toString() : "알 수 없는 파일 경로";
            throw new RuntimeException("이미지 처리 중 오류 발생: " + errorFilePath, e);
        }
    }

    private Map<String, String> insertOptionAndValue(String gdsNo, String userNo, String optionName, String choiceType, int order, String optNo, List<String> values) {
        Map<String, String> generatedOptionValueIds = new HashMap<>();

        ProductOption option = new ProductOption();
        option.setOptNo(optNo);
        option.setGdsNo(gdsNo);
        option.setCreatrNo(userNo);
        option.setMdfrNo(userNo);
        option.setOptNm(optionName);
        option.setSnglMtplChcSeCd(choiceType);
        option.setOptIndctSn(order);
        option.setRegDt(LocalDateTime.now());
        option.setActvtnYn(true);

        productsMapper.insertProductOption(option);

        for (String val : values) {
            ProductOptionValue value = new ProductOptionValue();
            String optVlNo = productsMapper.getMaxOptionValueNo();
            value.setOptVlNo(optVlNo);
            value.setOptNo(optNo);
            value.setCreatrNo(userNo);
            value.setMdfrNo(userNo);
            value.setVlNm(val);
            value.setRegDt(LocalDateTime.now());
            value.setActvtnYn(true);

            productsMapper.insertProductOptionValue(value);

            generatedOptionValueIds.put(val, optVlNo);
        }
        return generatedOptionValueIds;
    }

    @Override
    public boolean isProductCodeDuplicated(String productCode) {
        return productsMapper.countProductCode(productCode) > 0;
    }

    @Override
    public List<Products> getProductList() {
        return productsMapper.getProductList();
    }

    @Override
    public List<Products> getAllActiveProductsForCustomer() {
        return productsMapper.getAllActiveProductsForCustomer();
    }

    @Override
    public List<Products> getActiveProductsForCustomerByCategory(String categoryId) {
        return productsMapper.getActiveProductsForCustomerByCategory(categoryId);
    }

    @Override
    public Products getProductDetailWithImages(String gdsNo) {
        return productsMapper.getProductDetailWithImages(gdsNo);
    }

    // ⭐ getCategoryHierarchy 메서드 수정 ⭐
    /**
     * 카테고리 ID를 기반으로 해당 카테고리의 계층 정보를 조회합니다.
     * @param categoryId 조회할 카테고리 ID
     * @return 카테고리 계층 정보가 담긴 Map
     */
    @Override
    public Map<String, Object> getCategoryHierarchy(String categoryId) {
        Map<String, Object> result = new HashMap<>();
        // ProductMapper에서 카테고리 ID를 이용해 ProductCategory 객체를 조회합니다.
        ProductCategory currentCategory = productsMapper.getCategoryById(categoryId);
        result.put("currentCategory", currentCategory);

        if (currentCategory != null && currentCategory.getParentCategoryId() != null) {
            // 부모 카테고리가 있는 경우, 부모 카테고리 정보를 조회합니다.
            ProductCategory parentCategory = productsMapper.getCategoryById(currentCategory.getParentCategoryId());
            if (parentCategory != null) {
                result.put("parentCategoryName", parentCategory.getCategoryName());
                result.put("parentCategoryId", parentCategory.getCategoryId());
                // 부모 카테고리의 하위 카테고리(형제 카테고리)들을 조회합니다.
                result.put("subCategories", productsMapper.getSubCategoriesByParentId(parentCategory.getCategoryId()));
            }
        } else if (currentCategory != null) { // 최상위 카테고리인 경우 (부모가 없는 경우)
             result.put("parentCategoryName", currentCategory.getCategoryName());
             result.put("parentCategoryId", currentCategory.getCategoryId());
             // 자기 자신의 하위 카테고리들을 조회합니다.
             result.put("subCategories", productsMapper.getSubCategoriesByParentId(currentCategory.getCategoryId()));
        } else { // categoryId가 null이거나 찾을 수 없는 경우 (예: "전체"를 클릭하여 categoryId가 비어있을 때)
             result.put("parentCategoryName", "전체 상품"); // 기본값 설정
             result.put("parentCategoryId", null); // null 또는 특정 "모든 카테고리"를 나타내는 ID
             result.put("subCategories", productsMapper.getAllTopLevelCategories()); // 모든 최상위 카테고리 조회
        }
        return result;
    }

    @Override
    public String getTopLevelCategoryIdByName(String categoryName) {
        return productsMapper.getTopLevelCategoryIdByName(categoryName);
    }

    @Override
    public List<ProductCategory> getAllProductCategories() {
        return productsMapper.getAllProductCategories();
    }

    // --- ⭐ 이 두 메서드를 수정하여 페이지네이션 로직을 포함시킵니다 ⭐ ---
    @Override
    public Map<String, Object> getFilteredAndSortedProducts(String categoryId, String sortBy, Map<String, Object> filterParams, int currentPage) {
        Map<String, Object> paramMap = new HashMap<>(filterParams);
        paramMap.put("categoryId", categoryId);
        paramMap.put("sortBy", sortBy);

        // 1. 페이지네이션 파라미터 설정
        // 한 페이지에 보여줄 상품 수 (기본값 12, 필요에 따라 조정 가능)
        int pageSize = 12; 
        if (paramMap.containsKey("pageSize") && paramMap.get("pageSize") instanceof String) {
            try {
                pageSize = Integer.parseInt((String) paramMap.get("pageSize"));
            } catch (NumberFormatException e) {
                // 파싱 실패 시 기본값 유지
                pageSize = 12;
            }
        } else if (paramMap.containsKey("pageSize") && paramMap.get("pageSize") instanceof Integer) {
             pageSize = (int) paramMap.get("pageSize");
        }


        // Offset 계산: (현재 페이지 - 1) * 페이지 당 상품 수
        int offset = (currentPage - 1) * pageSize;
        if (offset < 0) offset = 0; // 음수 방지

        // paramMap에 페이지네이션 정보 추가
        paramMap.put("pageSize", pageSize);
        paramMap.put("offset", offset);
        paramMap.put("currentPage", currentPage);

        // 2. 총 상품 개수 조회 (페이지네이션 계산에 필요)
        long totalProductsCount = productsMapper.countFilteredProducts(paramMap);
        
        // 3. 전체 페이지 수 계산
        int lastPage = (int) Math.ceil((double) totalProductsCount / pageSize);
        if (lastPage == 0) lastPage = 1; // 상품이 0개일 때 최소 1페이지

        // 4. 상품 목록 조회
        List<Products> productsList = productsMapper.getFilteredAndSortedProducts(paramMap);

        // 결과 Map 구성
        Map<String, Object> result = new HashMap<>();
        result.put("productsList", productsList);
        result.put("totalProductsCount", totalProductsCount);
        result.put("currentPage", currentPage);
        result.put("lastPage", lastPage);
        result.put("pageSize", pageSize); // 뷰에서 활용할 수 있도록 추가
        result.put("offset", offset); // 뷰에서 활용할 수 있도록 추가

        return result;
    }

    // countFilteredProducts 메서드는 직접적으로 컨트롤러에서 호출되지 않고,
    // getFilteredAndSortedProducts 내부에서 호출되므로,
    // ProductsService 인터페이스에서는 제거하거나 private으로 변경하는 것을 고려할 수 있습니다.
    // 여기서는 인터페이스 메서드 오버라이드를 유지하되, 내부 로직은 getFilteredAndSortedProducts에서 처리하는 방식입니다.
    @Override
    public long countFilteredProducts(String categoryId, String sortBy, Map<String, Object> filterParams) {
        Map<String, Object> paramMap = new HashMap<>(filterParams);
        paramMap.put("categoryId", categoryId);
        paramMap.put("sortBy", sortBy);
        // 이 메서드는 getFilteredAndSortedProducts 내에서 호출될 때 이미 pageSize와 offset이 설정된 paramMap을 받습니다.
        // 또는, 이 메서드가 단독으로 호출될 경우 pageSize와 offset에 대한 기본 처리가 필요할 수 있습니다.
        // 현재 상황에서는 getFilteredAndSortedProducts에서 모든 페이지네이션 로직을 처리하는 것이 좋습니다.
        // 따라서 여기서는 단순히 paramMap을 전달하여 매퍼를 호출합니다.
        return productsMapper.countFilteredProducts(paramMap);
    }
    // --- ⭐ 위 두 메서드 수정 끝 ⭐ ---


    @Override
    public List<ProductOptionValue> getAllProductColors() {
        return productsMapper.getAllProductOptionValuesByType("색상"); // '컬러' 대신 '색상'으로 수정 (데이터베이스 값과 일치하게)
    }

    @Override
    public List<ProductOptionValue> getAllApparelSizes() {
        return productsMapper.getAllProductOptionValuesByType("의류 사이즈");
    }

    @Override
    public List<ProductOptionValue> getAllShoeSizes() {
        return productsMapper.getAllProductOptionValuesByType("신발 사이즈");
    }

    @Override
    public List<Stores> getAllBrands() {
        return productsMapper.getAllStores();
    }
}