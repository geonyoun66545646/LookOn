package ks55team02.seller.products.service.impl;

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
public class ProductsServiceImpl implements ProductsService {

    // --- 의존성 주입 ---
    private final ProductsMapper productsMapper;
    private final ProductOptionMapper productOptionMapper;
    private final StoreMapper storeMapper;
    private final FilesUtils filesUtils;

    // --- 상품 CUD (생성, 수정, 삭제) ---

    @Override
    @Transactional
    public void addProduct(ProductRegistrationRequest request) {
        // 1. 상품 PK 생성 및 기본 정보 설정
        String gdsNo = productsMapper.getMaxProductCode();
        request.setGdsNo(gdsNo);

        Products product = new Products();
        product.setGdsNo(gdsNo);
        product.setStoreId(request.getStoreId());
        product.setCtgryNo(StringUtils.hasText(request.getProductCategory2()) ? request.getProductCategory2() : request.getProductCategory1());
        product.setSelUserNo(request.getSelUserNo());
        product.setGdsNm(request.getProductName());
        product.setGdsExpln(request.getProductDescription());
        product.setBasPrc(request.getBasePrice() != null ? request.getBasePrice().intValue() : 0);
        product.setDscntRt(request.getDiscountRate());
        product.setMinPurchaseQty(request.getMinPurchase());
        product.setMaxPurchaseQty(request.getMaxPurchase());
        request.calculateFinalPrice();
        product.setLastSelPrc(request.getFinalPrice() != null ? request.getFinalPrice().intValue() : 0);
        product.setRegDt(LocalDateTime.now());
        // 신규 등록 시, 판매자에게는 보이고(활성), 구매자에게는 안보임(비노출/승인대기)
        product.setActvtnYn(true);
        product.setExpsrYn(false);
        productsMapper.addProduct(product);

        // 2. 이미지 파일 저장 (공통 헬퍼 메소드 호출)
        saveProductImages(request, gdsNo);
        
        // 3. 옵션 및 재고 정보 저장 (공통 헬퍼 메소드 호출)
        saveOptionsAndStock(request, gdsNo);
    }
    
    @Override
    @Transactional
    public void updateProduct(ProductRegistrationRequest request) {
        String gdsNo = request.getGdsNo();
        if (!StringUtils.hasText(gdsNo)) {
            throw new IllegalArgumentException("상품 수정을 위한 상품 ID가 없습니다.");
        }

        // 1. 상품 기본 정보 업데이트
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
        // TODO: 수정자 정보는 실제 로그인된 사용자 정보로 설정
        // product.setMdfrNo(loggedInUserId);
        productsMapper.updateProduct(product);

        // 2. 기존 연관 데이터 비활성화
        productsMapper.deactivateImagesByGdsNo(gdsNo);
        productsMapper.deactivateOptionsByGdsNo(gdsNo);
        productsMapper.deactivateStatusByGdsNo(gdsNo);
        
        // 3. 수정된 정보로 새로 INSERT
        saveProductImages(request, gdsNo);
        saveOptionsAndStock(request, gdsNo);
        
        // 4. 승인 상태 변경 로직 (향후 추가될 위치)
    }

    @Override
    public void deactivateProduct(String gdsNo, String selUserNo) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gdsNo", gdsNo);
        paramMap.put("selUserNo", selUserNo);
        productsMapper.deactivateProduct(paramMap);
    }

    // --- 상품 조회 (Read) ---

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

    // --- 유틸리티 및 필터용 옵션 조회 ---

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
    
    @Override
    public List<ProductOptionValue> getAllApparelSizes() {
    	List<ProductOptionValue> allSizes = productOptionMapper.getAllProductOptionValuesByType("사이즈");
        Pattern includePattern = Pattern.compile("^(XS|S|M|L|XL|2XL|\\d{2,3})$", Pattern.CASE_INSENSITIVE);
        List<Pattern> excludePatterns = List.of(
            Pattern.compile("^\\d{1,3}(\\.\\d)?$"),
            Pattern.compile("^(FREE|단일)$", Pattern.CASE_INSENSITIVE)
        );
        List<String> customOrder = List.of("XS", "S", "M", "L", "XL", "2XL");
        return processAndFilterAndSortSizes(allSizes, includePattern, excludePatterns, customOrder);
    }

    @Override
    public List<ProductOptionValue> getAllShoeSizes() {
    	List<ProductOptionValue> allSizes = productOptionMapper.getAllProductOptionValuesByType("사이즈");
        Pattern includePattern = Pattern.compile("^\\d{1,3}(\\.\\d)?$");
        List<Pattern> excludePatterns = List.of(
            Pattern.compile("^(XS|S|M|L|XL|2XL|\\d{2,3})$", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^(FREE|단일)$", Pattern.CASE_INSENSITIVE)
        );
        return processAndFilterAndSortSizes(allSizes, includePattern, excludePatterns, null);
    }

    @Override
    public List<ProductOptionValue> getAllFashionSizes() {
    	List<ProductOptionValue> allSizes = productOptionMapper.getAllProductOptionValuesByType("사이즈");
        Pattern includePattern = Pattern.compile("^(FREE|단일)$", Pattern.CASE_INSENSITIVE);
        List<String> customOrder = List.of("FREE", "단일");
        return processAndFilterAndSortSizes(allSizes, includePattern, null, customOrder);
    }

	@Override
	public List<Stores> getAllBrands() {
		return storeMapper.getAllStores();
	}

    // --- Private Helper Methods ---

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
        String selUserNo = request.getSelUserNo();

		int optOrder = 1;
        if (StringUtils.hasText(request.getGenderOption())) {
			genderOptionValueMap = insertOptionAndValue(gdsNo, selUserNo, "성별", "S", optOrder++, List.of(request.getGenderOption()));
		}
		if (request.getColorOptions() != null && !request.getColorOptions().isEmpty()) {
			List<String> uniqueColorOptions = request.getColorOptions().stream().distinct().collect(Collectors.toList());
			colorOptionValueMap = insertOptionAndValue(gdsNo, selUserNo, "색상", "S", optOrder++, uniqueColorOptions);
		}
		if (request.getSizeOptions() != null && !request.getSizeOptions().isEmpty()) {
			List<String> uniqueSizeOptions = request.getSizeOptions().stream().distinct().collect(Collectors.toList());
			sizeOptionValueMap = insertOptionAndValue(gdsNo, selUserNo, "사이즈", "S", optOrder++, uniqueSizeOptions);
		}

		if (request.getProductOptionCombinations() != null && !request.getProductOptionCombinations().isEmpty()) {
			for (ProductCombinationData combinationData : request.getProductOptionCombinations()) {
				String gdsSttsNo = productsMapper.getMaxStatusNo();
				insertProductStatus(gdsSttsNo, gdsNo, selUserNo, combinationData.getQuantity());

				String genderOptValueId = genderOptionValueMap.get(combinationData.getOptVlNo1());
				String colorOptValueId = colorOptionValueMap.get(combinationData.getOptVlNo2());
				String sizeOptValueId = sizeOptionValueMap.get(combinationData.getOptVlNo3());

				if (StringUtils.hasText(genderOptValueId)) insertStatusOptionMapping(gdsSttsNo, genderOptValueId, selUserNo);
				if (StringUtils.hasText(colorOptValueId)) insertStatusOptionMapping(gdsSttsNo, colorOptValueId, selUserNo);
				if (StringUtils.hasText(sizeOptValueId)) insertStatusOptionMapping(gdsSttsNo, sizeOptValueId, selUserNo);
			}
		} else { 
			String gdsSttsNo = productsMapper.getMaxStatusNo();
			insertProductStatus(gdsSttsNo, gdsNo, selUserNo, request.getStockQuantity());
		}
    }
    
	private Map<String, String> insertOptionAndValue(String gdsNo, String userNo, String optionName, String choiceType, int order, List<String> values) {
		Map<String, String> generatedOptionValueIds = new HashMap<>();
        String optNo = productsMapper.getMaxOptionNo();

		ProductOption option = new ProductOption();
		option.setOptNo(optNo);
		option.setGdsNo(gdsNo);
		option.setCreatrNo(userNo);
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
			value.setCreatrNo(userNo);
			value.setVlNm(val);
			value.setActvtnYn(true);
			productsMapper.insertProductOptionValue(value);
			generatedOptionValueIds.put(val, optVlNo);
		}
		return generatedOptionValueIds;
	}

    private void insertProductStatus(String gdsSttsNo, String gdsNo, String selUserNo, Integer quantity) {
        ProductStatus productStatus = new ProductStatus();
        productStatus.setGdsSttsNo(gdsSttsNo);
        productStatus.setGdsNo(gdsNo);
        productStatus.setCreatrNo(selUserNo);
        int stock = (quantity != null) ? quantity : 0;
        productStatus.setSelPsbltyQntty(stock);
        productStatus.setSldoutYn(stock <= 0);
        productStatus.setActvtnYn(true);
        productsMapper.insertProductStatus(productStatus);
    }
    
    private void insertStatusOptionMapping(String gdsSttsNo, String optVlNo, String selUserNo) {
        StatusOptionMapping mapping = new StatusOptionMapping();
        mapping.setGdsSttsNo(gdsSttsNo);
        mapping.setOptVlNo(optVlNo);
        mapping.setCreatrNo(selUserNo);
        mapping.setActvtnYn(true);
        productsMapper.insertStatusOptionMapping(mapping);
    }

    private List<ProductOptionValue> processAndFilterAndSortSizes(
            List<ProductOptionValue> allSizes, Pattern includePattern,
            List<Pattern> excludePatterns, List<String> customOrder) {
        Set<String> seenVlNames = new HashSet<>();
        List<ProductOptionValue> filteredAndUnique = new ArrayList<>();
        if (allSizes == null) return filteredAndUnique;
        for (ProductOptionValue pov : allSizes) {
            String trimmedVlNm = pov.getVlNm().trim();
            if (includePattern != null && !includePattern.matcher(trimmedVlNm).matches()) continue;
            boolean excluded = false;
            if (excludePatterns != null) {
                for (Pattern excludePattern : excludePatterns) {
                    if (excludePattern.matcher(trimmedVlNm).matches()) {
                        excluded = true;
                        break;
                    }
                }
            }
            if (excluded) continue;
            if (seenVlNames.add(trimmedVlNm)) filteredAndUnique.add(pov);
        }
        filteredAndUnique.sort((s1, s2) -> {
            String s1Val = s1.getVlNm().trim();
            String s2Val = s2.getVlNm().trim();
            if (customOrder != null && !customOrder.isEmpty()) {
                int index1 = customOrder.indexOf(s1Val.toUpperCase());
                int index2 = customOrder.indexOf(s2Val.toUpperCase());
                if (index1 != -1 && index2 != -1) return Integer.compare(index1, index2);
                if (index1 != -1) return -1;
                if (index2 != -1) return 1;
            }
            try {
                return Double.compare(Double.parseDouble(s1Val), Double.parseDouble(s2Val));
            } catch (NumberFormatException e) {
                return s1Val.compareTo(s2Val);
            }
        });
        return filteredAndUnique;
    }
}