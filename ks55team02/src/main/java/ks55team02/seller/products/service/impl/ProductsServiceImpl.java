package ks55team02.seller.products.service.impl;

import ks55team02.seller.products.domain.*;
import ks55team02.seller.products.domain.ProductRegistrationRequest.ProductCombinationData;
import ks55team02.seller.products.mapper.ProductOptionMapper;
import ks55team02.seller.products.mapper.ProductsMapper;
import ks55team02.seller.products.service.ProductsService;
import ks55team02.seller.stores.domain.Stores;
import ks55team02.seller.stores.mapper.StoreMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductsServiceImpl implements ProductsService {

	private final ProductsMapper productsMapper;
	private final ProductOptionMapper productOptionMapper;
	private final StoreMapper storeMapper;
	
	// ProductsServiceImpl.java에 추가
    @Override
    public void deactivateProduct(String gdsNo, String selUserNo) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gdsNo", gdsNo);
        paramMap.put("selUserNo", selUserNo); // 나중에 권한 체크 등에 활용 가능
        
        productsMapper.deactivateProduct(paramMap);
    }
	
    @Override
    @Transactional
    public void updateProduct(ProductRegistrationRequest request) {
        String gdsNo = request.getGdsNo();
        if (!StringUtils.hasText(gdsNo)) {
            throw new IllegalArgumentException("상품 수정을 위한 상품 ID가 없습니다.");
        }

        // --- 1. 상품 기본 정보 업데이트 ---
        Products product = new Products();
        product.setGdsNo(gdsNo);
        
        String finalCategoryNo = StringUtils.hasText(request.getProductCategory2()) ?
                                 request.getProductCategory2() : request.getProductCategory1();
        product.setCtgryNo(finalCategoryNo);
        
        product.setGdsNm(request.getProductName());
        product.setGdsExpln(request.getProductDescription());
        product.setBasPrc(request.getBasePrice() != null ? request.getBasePrice().intValue() : 0);
        product.setDscntRt(request.getDiscountRate());
        product.setMinPurchaseQty(request.getMinPurchase());
        product.setMaxPurchaseQty(request.getMaxPurchase());
        request.calculateFinalPrice();
        product.setLastSelPrc(request.getFinalPrice() != null ? request.getFinalPrice().intValue() : 0);
        
        // TODO: 수정자 정보는 실제 로그인된 사용자 정보로 설정해야 합니다.
        // product.setMdfrNo(loggedInUserId);
        
        productsMapper.updateProduct(product);

        // --- 2. 기존 연관 데이터(이미지, 옵션, 재고) 비활성화 ---
        // 기존 데이터를 모두 비활성화(actvtn_yn = false) 처리합니다.
        productsMapper.deactivateImagesByGdsNo(gdsNo);
        productsMapper.deactivateOptionsByGdsNo(gdsNo);
        productsMapper.deactivateStatusByGdsNo(gdsNo);
        
        // --- 3. 수정된 정보로 새로 INSERT (addProduct 로직 재활용) ---
        
        // 3-1. 이미지 새로 삽입
        int imgIndctSn = 1;
        if (request.getThumbnailImage() != null) {
			for (MultipartFile file : request.getThumbnailImage()) {
				if(!file.isEmpty()) saveAndInsertImage(file, gdsNo, request.getSelUserNo(), productsMapper.getMaxImageNo(), imgIndctSn++, ProductImageType.THUMBNAIL);
			}
		}
		if (request.getMainImage() != null) {
			for (MultipartFile file : request.getMainImage()) {
				if(!file.isEmpty()) saveAndInsertImage(file, gdsNo, request.getSelUserNo(), productsMapper.getMaxImageNo(), imgIndctSn++, ProductImageType.MAIN);
			}
		}
		if (request.getDetailImage() != null) {
			for (MultipartFile file : request.getDetailImage()) {
				if(!file.isEmpty()) saveAndInsertImage(file, gdsNo, request.getSelUserNo(), productsMapper.getMaxImageNo(), imgIndctSn++, ProductImageType.DETAIL);
			}
		}
        // TODO: 기존에 업로드된 이미지를 그대로 사용하는 경우에 대한 처리가 필요합니다.
        // (예: 프론트에서 기존 이미지 URL 목록을 hidden input으로 함께 보내고,
        //  새로 업로드된 파일이 없을 경우 이 URL을 기반으로 DB에 다시 insert)

        // 3-2. 옵션 및 재고 새로 삽입
		Map<String, String> genderOptionValueMap = new HashMap<>();
		Map<String, String> colorOptionValueMap = new HashMap<>();
		Map<String, String> sizeOptionValueMap = new HashMap<>();

		int optOrder = 1;
        if (StringUtils.hasText(request.getGenderOption())) {
			String genderOptNo = productsMapper.getMaxOptionNo();
			genderOptionValueMap = insertOptionAndValue(gdsNo, request.getSelUserNo(), "성별", "S", optOrder++, genderOptNo, List.of(request.getGenderOption()));
		}
		if (request.getColorOptions() != null && !request.getColorOptions().isEmpty()) {
			String colorOptNo = productsMapper.getMaxOptionNo();
			List<String> uniqueColorOptions = request.getColorOptions().stream().distinct().collect(Collectors.toList());
			colorOptionValueMap = insertOptionAndValue(gdsNo, request.getSelUserNo(), "색상", "S", optOrder++, colorOptNo, uniqueColorOptions);
		}
		if (request.getSizeOptions() != null && !request.getSizeOptions().isEmpty()) {
			String sizeOptNo = productsMapper.getMaxOptionNo();
			List<String> uniqueSizeOptions = request.getSizeOptions().stream().distinct().collect(Collectors.toList());
			sizeOptionValueMap = insertOptionAndValue(gdsNo, request.getSelUserNo(), "사이즈", "S", optOrder++, sizeOptNo, uniqueSizeOptions);
		}

		if (request.getProductOptionCombinations() != null && !request.getProductOptionCombinations().isEmpty()) {
			for (ProductRegistrationRequest.ProductCombinationData combinationData : request.getProductOptionCombinations()) {
				String gdsSttsNo = productsMapper.getMaxStatusNo();
				insertProductStatus(gdsSttsNo, gdsNo, request.getSelUserNo(), combinationData.getQuantity());

				String genderOptValueId = genderOptionValueMap.get(combinationData.getOptVlNo1());
				String colorOptValueId = colorOptionValueMap.get(combinationData.getOptVlNo2());
				String sizeOptValueId = sizeOptionValueMap.get(combinationData.getOptVlNo3());

				if (StringUtils.hasText(genderOptValueId)) insertStatusOptionMapping(gdsSttsNo, genderOptValueId, request.getSelUserNo());
				if (StringUtils.hasText(colorOptValueId)) insertStatusOptionMapping(gdsSttsNo, colorOptValueId, request.getSelUserNo());
				if (StringUtils.hasText(sizeOptValueId)) insertStatusOptionMapping(gdsSttsNo, sizeOptValueId, request.getSelUserNo());
			}
		} else { // 단일 상품 재고
			String gdsSttsNo = productsMapper.getMaxStatusNo();
			insertProductStatus(gdsSttsNo, gdsNo, request.getSelUserNo(), request.getStockQuantity());
		}
    }
	
	
	@Value("${product.upload.dir}")
	private String uploadDir;

	// 판매자별 상품 목록 조회
	@Override
	public List<Products> getProductsBySellerAndStore(String selUserNo, String storeId) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("selUserNo", selUserNo);
		paramMap.put("storeId", storeId);
		return productsMapper.getProductsBySellerAndStore(paramMap);
	}

	// 신규 상품 등록 (이미지, 옵션, 재고 포함)
	@Override
	@Transactional
	public void addProduct(ProductRegistrationRequest request) {
		String selUserNo = request.getSelUserNo();
		String storeId = request.getStoreId();

		// 1. 상품 PK 생성 및 기본 정보 설정
		String gdsNo = productsMapper.getMaxProductCode();
		request.setGdsNo(gdsNo);

		Products product = new Products();
		product.setGdsNo(gdsNo);
		product.setStoreId(storeId);

		String finalCategoryNo = StringUtils.hasText(request.getProductCategory2()) ?
								 request.getProductCategory2() : request.getProductCategory1();
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

		// 2. 이미지 파일 저장 및 DB 정보 삽입
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

		// 3. 옵션 및 옵션 값 정보 생성 및 저장 (중복 제거)
		Map<String, String> genderOptionValueMap = new HashMap<>();
		Map<String, String> colorOptionValueMap = new HashMap<>();
		Map<String, String> sizeOptionValueMap = new HashMap<>();

		int optOrder = 1;
		if (StringUtils.hasText(request.getGenderOption())) {
			String genderOptNo = productsMapper.getMaxOptionNo();
			genderOptionValueMap = insertOptionAndValue(gdsNo, selUserNo, "성별", "S", optOrder++, genderOptNo, List.of(request.getGenderOption()));
		}
		if (request.getColorOptions() != null && !request.getColorOptions().isEmpty()) {
			String colorOptNo = productsMapper.getMaxOptionNo();
			List<String> uniqueColorOptions = request.getColorOptions().stream().distinct().collect(Collectors.toList());
			colorOptionValueMap = insertOptionAndValue(gdsNo, selUserNo, "색상", "S", optOrder++, colorOptNo, uniqueColorOptions);
		}
		if (request.getSizeOptions() != null && !request.getSizeOptions().isEmpty()) {
			String sizeOptNo = productsMapper.getMaxOptionNo();
			List<String> uniqueSizeOptions = request.getSizeOptions().stream().distinct().collect(Collectors.toList());
			sizeOptionValueMap = insertOptionAndValue(gdsNo, selUserNo, "사이즈", "S", optOrder++, sizeOptNo, uniqueSizeOptions);
		}

		// 4. 옵션 조합별 재고 및 상태 정보 저장
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
		} else { // 옵션이 없는 단일 상품의 경우
			String gdsSttsNo = productsMapper.getMaxStatusNo();
			insertProductStatus(gdsSttsNo, gdsNo, selUserNo, request.getStockQuantity());
		}
	}
    
    // 상품 코드 중복 확인
	@Override
	public boolean isProductCodeDuplicated(String productCode) {
		return productsMapper.countProductCode(productCode) > 0;
	}

	// (관리자용) 전체 상품 목록 조회
	@Override
	public List<Products> getProductList() {
		return productsMapper.getProductList();
	}

	// 상품 상세 정보 조회
	@Override
	public Products getProductDetailWithImages(String gdsNo) {
		return productsMapper.getProductDetailWithImages(gdsNo);
	}

	// 필터용 전체 색상 목록 조회
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

	// 필터용 의류 사이즈 목록 조회
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

	// 필터용 신발 사이즈 목록 조회
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

	// 필터용 패션잡화 사이즈 목록 조회
    @Override
    public List<ProductOptionValue> getAllFashionSizes() {
    	List<ProductOptionValue> allSizes = productOptionMapper.getAllProductOptionValuesByType("사이즈");
        Pattern includePattern = Pattern.compile("^(FREE|단일)$", Pattern.CASE_INSENSITIVE);
        List<String> customOrder = List.of("FREE", "단일");
        return processAndFilterAndSortSizes(allSizes, includePattern, null, customOrder);
    }

	// 필터용 전체 브랜드(상점) 목록 조회
	@Override
	public List<Stores> getAllBrands() {
		return storeMapper.getAllStores();
	}

    // --- Helper Methods ---
    
    // 이미지 파일 저장 및 DB 삽입 헬퍼 메소드
	private void saveAndInsertImage(MultipartFile file, String gdsNo, String creatorNo, String imgNo, int imgIndctSn, ProductImageType imageType) {
		if (file == null || file.isEmpty()) return;

		try {
			Path uploadPath = Paths.get(uploadDir);
			if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

			String ext = "";
			String originalFilename = file.getOriginalFilename();
			if (originalFilename != null && originalFilename.contains(".")) {
				ext = originalFilename.substring(originalFilename.lastIndexOf("."));
			}
			String savedFileName = imgNo + ext;
			Path filePath = uploadPath.resolve(savedFileName);
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
			throw new RuntimeException("이미지 처리 중 오류 발생: " + file.getOriginalFilename(), e);
		}
	}

    // 옵션 및 옵션 값 생성 헬퍼 메소드
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

    // 상품 상태(재고) 정보 생성 헬퍼 메소드
    private void insertProductStatus(String gdsSttsNo, String gdsNo, String selUserNo, Integer quantity) {
        ProductStatus productStatus = new ProductStatus();
        productStatus.setGdsSttsNo(gdsSttsNo);
        productStatus.setGdsNo(gdsNo);
        productStatus.setCreatrNo(selUserNo);
        productStatus.setMdfrNo(selUserNo);
        
        int stock = (quantity != null) ? quantity : 0;
        productStatus.setSelPsbltyQntty(stock);
        productStatus.setSldoutYn(stock <= 0);
        
        productStatus.setRegDt(LocalDateTime.now());
        productStatus.setActvtnYn(true);
        productsMapper.insertProductStatus(productStatus);
    }
    
    // 상태-옵션 매핑 정보 생성 헬퍼 메소드
    private void insertStatusOptionMapping(String gdsSttsNo, String optVlNo, String selUserNo) {
        StatusOptionMapping mapping = new StatusOptionMapping();
        mapping.setGdsSttsNo(gdsSttsNo);
        mapping.setOptVlNo(optVlNo);
        mapping.setCreatrNo(selUserNo);
        mapping.setCrtYmd(LocalDateTime.now());
        mapping.setActvtnYn(true);
        productsMapper.insertStatusOptionMapping(mapping);
    }

    // 사이즈 옵션 필터링 및 정렬 헬퍼 메소드
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