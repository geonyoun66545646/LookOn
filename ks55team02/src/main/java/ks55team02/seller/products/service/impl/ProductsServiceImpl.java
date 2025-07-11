package ks55team02.seller.products.service.impl;

import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.ProductApprovalHistory;
import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.mapper.AdminProductManagementMapper;
import ks55team02.seller.products.domain.*;
import ks55team02.seller.products.domain.ProductRegistrationRequest.ProductCombinationData;
import ks55team02.seller.products.mapper.ProductOptionMapper;
import ks55team02.seller.products.mapper.ProductsMapper;
import ks55team02.seller.products.service.ProductsService;
import ks55team02.seller.stores.domain.Stores;
import ks55team02.seller.stores.mapper.StoreMapper;
import ks55team02.util.FileDetail;
import ks55team02.util.FilesUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductsServiceImpl implements ProductsService {

    private final ProductsMapper productsMapper;
    private final ProductOptionMapper productOptionMapper;
    private final StoreMapper storeMapper;
    private final FilesUtils filesUtils;
    private final AdminProductManagementMapper adminProductManagementMapper;

    @Override
    public List<Stores> searchBrands(String keyword) {
        return storeMapper.searchStoresByKeyword(keyword);
    }
    
    @Override
    @Transactional
    public void addProduct(ProductRegistrationRequest request) {
        String gdsNo = productsMapper.getMaxProductCode();
        request.setGdsNo(gdsNo);

        Products product = new Products();
        product.setGdsNo(gdsNo);
        product.setStoreId(request.getStoreId());
        product.setCtgryNo(StringUtils.hasText(request.getProductCategory2()) ? request.getProductCategory2() : request.getProductCategory1());
        product.setSelUserNo(request.getSelUserNo());
        product.setCreatrNo(request.getSelUserNo());
        product.setGdsNm(request.getProductName());
        product.setGdsExpln(request.getProductDescription());
        product.setBasPrc(request.getBasePrice() != null ? request.getBasePrice().intValue() : 0);
        product.setDscntRt(request.getDiscountRate());
        product.setMinPurchaseQty(request.getMinPurchase());
        product.setMaxPurchaseQty(request.getMaxPurchase());
        request.calculateFinalPrice();
        product.setLastSelPrc(request.getFinalPrice() != null ? request.getFinalPrice().intValue() : 0);
        product.setRegDt(LocalDateTime.now());
        product.setActvtnYn(true);
        product.setExpsrYn(false);
        productsMapper.addProduct(product);

        // ⭐ 상품 등록 시 'aprv_rjct_cycl'은 항상 1로 시작 ⭐
        String newHistoryCode = productsMapper.getMaxApprovalHistoryCode();
        ProductApprovalHistory initialHistory = new ProductApprovalHistory();
        initialHistory.setAprvRjctHstryCd(newHistoryCode);
        initialHistory.setGdsNo(gdsNo);
        initialHistory.setPrcsMngrId(request.getSelUserNo()); // 판매자 ID를 처리자로 설정
        initialHistory.setAprvSttsCd("대기");
        initialHistory.setPrcsDt(LocalDateTime.now());
        initialHistory.setAprvRjctCycl(1); // 최초 등록은 무조건 1차수
        initialHistory.setMngrCmntCn("판매자 상품 등록으로 인한 최초 승인 대기");
        adminProductManagementMapper.insertProductApprovalHistory(initialHistory);

        saveProductImages(request, gdsNo);
        saveOptionsAndStock(request, gdsNo);
    }

    @Override
    @Transactional
    public void updateProduct(ProductRegistrationRequest request) {
        String gdsNo = request.getGdsNo();
        if (!StringUtils.hasText(gdsNo)) {
            throw new IllegalArgumentException("상품 수정을 위한 상품 ID가 없습니다.");
        }

        String selUserNo = request.getSelUserNo();

        // 1. 상품 기본 정보 업데이트 (현재 코드와 동일)
        Products product = new Products();
        product.setGdsNo(gdsNo);
        product.setCtgryNo(StringUtils.hasText(request.getProductCategory2()) ? request.getProductCategory2() : request.getProductCategory1());
        product.setGdsNm(request.getProductName());
        product.setGdsExpln(request.getProductDescription());
        product.setBasPrc(request.getBasePrice() != null ? request.getBasePrice().intValue() : 0);
        product.setDscntRt(request.getDiscountRate());
        product.setMinPurchaseQty(request.getMinPurchase());
        product.setMaxPurchaseQty(request.getMaxPurchase());
        request.calculateFinalPrice();
        product.setLastSelPrc(request.getFinalPrice() != null ? request.getFinalPrice().intValue() : 0);
        product.setMdfrNo(selUserNo);
        productsMapper.updateProduct(product);

        // 2. 기존 연관 데이터 비활성화 (현재 코드와 동일)
        productsMapper.deactivateImagesByGdsNo(gdsNo);
        productsMapper.deactivateOptionsByGdsNo(gdsNo);
        productsMapper.deactivateStatusByGdsNo(gdsNo);

        // 3. 수정된 정보로 새로 INSERT (현재 코드와 동일)
        saveProductImages(request, gdsNo);
        saveOptionsAndStock(request, gdsNo);

        // 4. 재승인 절차: 새로운 차수 계산 방식 변경
        // ⭐ ⭐ ⭐ 이 부분 수정 ⭐ ⭐ ⭐
        // 판매자가 수정 제출하는 경우 (재승인 요청), 이는 새로운 "시도"를 의미합니다.
        // 따라서 기존의 승인/반려 이력 중 가장 높은 차수에 +1을 해야 합니다.
        int newCycle = adminProductManagementMapper.getLatestApprovalCycle(gdsNo) + 1;

        String newHistoryCode = productsMapper.getMaxApprovalHistoryCode();
        ProductApprovalHistory history = new ProductApprovalHistory();
        history.setAprvRjctHstryCd(newHistoryCode);
        history.setGdsNo(gdsNo);
        history.setPrcsMngrId(selUserNo);
        history.setAprvSttsCd("대기");
        history.setPrcsDt(LocalDateTime.now());
        history.setAprvRjctCycl(newCycle); // 새로운 시도이므로 기존 차수 + 1
        history.setMngrCmntCn("판매자 수정 요청으로 인한 재승인 대기 중");
        adminProductManagementMapper.insertProductApprovalHistory(history);
        log.info(">>>>>> [Service][updateProduct] 상품 수정으로 인한 새 '대기' 이력 생성. gdsNo: {}, Cycle: {}", gdsNo, newCycle);

        // 상품 노출 상태를 false로 변경 (현재 코드와 동일)
        Map<String, Object> productParams = new HashMap<>();
        productParams.put("gdsNo", gdsNo);
        productParams.put("managerId", selUserNo);
        productParams.put("exposure", false);
        adminProductManagementMapper.updateProductExposure(productParams);
    }

    @Override
    public void deactivateProduct(String gdsNo, String selUserNo) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gdsNo", gdsNo);
        paramMap.put("selUserNo", selUserNo);
        productsMapper.deactivateProduct(paramMap);
    }

    @Override
    public List<Products> getProductsBySellerAndStore(String selUserNo, String storeId) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("selUserNo", selUserNo);
        paramMap.put("storeId", storeId);
        return productsMapper.getProductsBySellerAndStore(paramMap);
    }

    @Override
    public List<Products> getProductList() {
        return productsMapper.getProductList();
    }

    @Override
    public Products getProductDetailWithImages(String gdsNo) {
        return productsMapper.getProductDetailWithImages(gdsNo);
    }

    @Override
    public boolean isProductCodeDuplicated(String productCode) {
        return productsMapper.countProductCode(productCode) > 0;
    }

    @Override
    public List<ProductOptionValue> getAllProductColors() {
         List<String> rawColorNames = productOptionMapper.getDistinctProductOptionValueNamesByType("색상");
        Set<String> uniqueColorNames = new HashSet<>();
        if (rawColorNames != null) {
            for (String colorName : rawColorNames) {
                uniqueColorNames.add(colorName.trim());
            }
        }
        List<String> sortedUniqueColorNames = uniqueColorNames.stream().sorted().collect(Collectors.toList());
        List<ProductOptionValue> colorOptions = new ArrayList<>();
        for (String colorName : sortedUniqueColorNames) {
            ProductOptionValue pov = new ProductOptionValue();
            pov.setVlNm(colorName);
            colorOptions.add(pov);
        }
        return colorOptions;
    }
    
 // ProductsServiceImpl.java

    // 필터용 의류 사이즈 목록 조회
    @Override
    public List<ProductOptionValue> getAllApparelSizes() {
        List<ProductOptionValue> allSizes = productOptionMapper.getAllProductOptionValuesByType("사이즈");
        List<String> apparelSizeOrder = List.of("XS", "S", "M", "L", "XL", "2XL", "3XL");

        if (allSizes == null) return new ArrayList<>();

        // vlNm을 기준으로 중복을 제거하고, 정해진 순서대로 정렬합니다.
        return allSizes.stream()
                // 1. vlNm이 null이 아니고, 의류 사이즈 목록에 포함된 값만 필터링
                .filter(pov -> pov.getVlNm() != null && apparelSizeOrder.contains(pov.getVlNm().toUpperCase()))
                // 2. vlNm 기준으로 중복 제거 (Map을 활용)
                .collect(Collectors.toMap(
                        pov -> pov.getVlNm().toUpperCase(), // Key: 사이즈 이름 (대소문자 무관)
                        pov -> pov,                          // Value: 첫 번째로 발견된 객체
                        (existing, replacement) -> existing   // 중복 키 발생 시 기존 값 유지
                ))
                .values().stream() // 3. 중복 제거된 Map의 값들(ProductOptionValue 객체)만 다시 스트림으로
                // 4. 정해진 순서대로 정렬
                .sorted(Comparator.comparingInt(pov -> apparelSizeOrder.indexOf(pov.getVlNm().toUpperCase())))
                .collect(Collectors.toList());
    }

	// 필터용 신발 사이즈 목록 조회
    @Override
    public List<ProductOptionValue> getAllShoeSizes() {
        List<ProductOptionValue> allSizes = productOptionMapper.getAllProductOptionValuesByType("사이즈");

        if (allSizes == null) return new ArrayList<>();
        
        return allSizes.stream()
                .filter(pov -> pov.getVlNm() != null && pov.getVlNm().matches("^[0-9]+(\\.[0-9])?$"))
                .collect(Collectors.toMap(
                        ProductOptionValue::getVlNm,
                        pov -> pov,
                        (existing, replacement) -> existing
                ))
                .values().stream()
                .sorted(Comparator.comparingDouble(pov -> Double.parseDouble(pov.getVlNm())))
                .collect(Collectors.toList());
    }

	// 필터용 패션잡화 사이즈 목록 조회
    @Override
    public List<ProductOptionValue> getAllFashionSizes() {
        List<ProductOptionValue> allSizes = productOptionMapper.getAllProductOptionValuesByType("사이즈");
        List<String> fashionSizeOrder = List.of("FREE", "단일");
        
        if (allSizes == null) return new ArrayList<>();

        return allSizes.stream()
                .filter(pov -> pov.getVlNm() != null && fashionSizeOrder.contains(pov.getVlNm().toUpperCase()))
                .collect(Collectors.toMap(
                        pov -> pov.getVlNm().toUpperCase(),
                        pov -> pov,
                        (existing, replacement) -> existing
                ))
                .values().stream()
                .sorted(Comparator.comparingInt(pov -> fashionSizeOrder.indexOf(pov.getVlNm().toUpperCase())))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Stores> getAllBrands() {
        return storeMapper.getAllStores();
    }

    private void saveProductImages(ProductRegistrationRequest request, String gdsNo) {
        int imgIndctSn = 1;
        imgIndctSn = saveAndRegisterImage(request.getThumbnailImage(), gdsNo, request.getSelUserNo(), imgIndctSn, ProductImageType.THUMBNAIL);
        imgIndctSn = saveAndRegisterImage(request.getMainImage(), gdsNo, request.getSelUserNo(), imgIndctSn, ProductImageType.MAIN);
        saveAndRegisterImage(request.getDetailImage(), gdsNo, request.getSelUserNo(), imgIndctSn, ProductImageType.DETAIL);
    }

    private int saveAndRegisterImage(List<MultipartFile> files, String gdsNo, String creatorNo, int startSn, ProductImageType imageType) {
        if (files == null || files.isEmpty()) return startSn;

        int currentSn = startSn;
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                FileDetail fileDetail = filesUtils.saveFile(file, "products");
                if (fileDetail != null) {
                    ProductImage productImage = new ProductImage();
                    productImage.setImgNo(productsMapper.getMaxImageNo());
                    productImage.setGdsNo(gdsNo);
                    productImage.setCreatrNo(creatorNo);
                    productImage.setImgFilePathNm(fileDetail.getSavedPath());
                    productImage.setImgIndctSn(currentSn++);
                    productImage.setImgType(imageType);
                    productImage.setActvtnYn(true);
                    productsMapper.insertProductImage(productImage);
                }
            }
        }
        return currentSn;
    }

    private void saveOptionsAndStock(ProductRegistrationRequest request, String gdsNo) {
        Map<String, String> genderOptionValueMap = new HashMap<>();
        Map<String, String> colorOptionValueMap = new HashMap<>();
        Map<String, String> sizeOptionValueMap = new HashMap<>();
        String creatorNo = request.getSelUserNo();

        int optOrder = 1;
        if (StringUtils.hasText(request.getGenderOption())) {
            genderOptionValueMap = insertOptionAndValue(gdsNo, creatorNo, "성별", "S", optOrder++, List.of(request.getGenderOption()));
        }
        if (request.getColorOptions() != null && !request.getColorOptions().isEmpty()) {
            List<String> uniqueColorOptions = request.getColorOptions().stream().distinct().collect(Collectors.toList());
            colorOptionValueMap = insertOptionAndValue(gdsNo, creatorNo, "색상", "S", optOrder++, uniqueColorOptions);
        }
        if (request.getSizeOptions() != null && !request.getSizeOptions().isEmpty()) {
            List<String> uniqueSizeOptions = request.getSizeOptions().stream().distinct().collect(Collectors.toList());
            sizeOptionValueMap = insertOptionAndValue(gdsNo, creatorNo, "사이즈", "S", optOrder++, uniqueSizeOptions);
        }

        if (request.getProductOptionCombinations() != null && !request.getProductOptionCombinations().isEmpty()) {
            for (ProductCombinationData combinationData : request.getProductOptionCombinations()) {
                String gdsSttsNo = productsMapper.getMaxStatusNo();
                insertProductStatus(gdsSttsNo, gdsNo, creatorNo, combinationData.getQuantity());

                String genderOptValueId = genderOptionValueMap.get(combinationData.getOptVlNo1());
                String colorOptValueId = colorOptionValueMap.get(combinationData.getOptVlNo2());
                String sizeOptValueId = sizeOptionValueMap.get(combinationData.getOptVlNo3());

                if (StringUtils.hasText(genderOptValueId)) insertStatusOptionMapping(gdsSttsNo, genderOptValueId, creatorNo);
                if (StringUtils.hasText(colorOptValueId)) insertStatusOptionMapping(gdsSttsNo, colorOptValueId, creatorNo);
                if (StringUtils.hasText(sizeOptValueId)) insertStatusOptionMapping(gdsSttsNo, sizeOptValueId, creatorNo);
            }
        } else {
            String gdsSttsNo = productsMapper.getMaxStatusNo();
            insertProductStatus(gdsSttsNo, gdsNo, creatorNo, request.getStockQuantity());
        }
    }

    private Map<String, String> insertOptionAndValue(String gdsNo, String creatorNo, String optionName, String choiceType, int order, List<String> values) {
        Map<String, String> generatedOptionValueIds = new HashMap<>();
        String optNo = productsMapper.getMaxOptionNo();

        ProductOption option = new ProductOption();
        option.setOptNo(optNo);
        option.setGdsNo(gdsNo);
        option.setCreatrNo(creatorNo);
        option.setOptNm(optionName);
        option.setSnglMtplChcSeCd(choiceType);
        option.setOptIndctSn(order);
        option.setActvtnYn(true);
        productsMapper.insertProductOption(option);

        for (String val : values) {
            ProductOptionValue value = new ProductOptionValue();
            String optVlNo = productsMapper.getMaxOptionValueNo();
            value.setOptVlNo(optVlNo);
            value.setOptNo(optNo);
            value.setCreatrNo(creatorNo);
            value.setVlNm(val);
            value.setActvtnYn(true);
            productsMapper.insertProductOptionValue(value);
            generatedOptionValueIds.put(val, optVlNo);
        }
        return generatedOptionValueIds;
    }

    private void insertProductStatus(String gdsSttsNo, String gdsNo, String creatorNo, Integer quantity) {
        ProductStatus productStatus = new ProductStatus();
        productStatus.setGdsSttsNo(gdsSttsNo);
        productStatus.setGdsNo(gdsNo);
        productStatus.setCreatrNo(creatorNo);
        int stock = (quantity != null) ? quantity : 0;
        productStatus.setSelPsbltyQntty(stock);
        productStatus.setSldoutYn(stock <= 0);
        productStatus.setActvtnYn(true);
        productsMapper.insertProductStatus(productStatus);
    }

    private void insertStatusOptionMapping(String gdsSttsNo, String optVlNo, String creatorNo) {
        StatusOptionMapping mapping = new StatusOptionMapping();
        mapping.setGdsSttsNo(gdsSttsNo);
        mapping.setOptVlNo(optVlNo);
        mapping.setCreatrNo(creatorNo);
        mapping.setActvtnYn(true);
        productsMapper.insertStatusOptionMapping(mapping);
    }
}