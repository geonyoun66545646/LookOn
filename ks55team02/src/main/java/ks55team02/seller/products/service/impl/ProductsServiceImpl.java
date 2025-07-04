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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet; // Set을 위한 임포트
import java.util.List;
import java.util.Map;
import java.util.Set; // Set을 위한 임포트
import java.util.regex.Pattern; // Pattern을 위한 임포트
import java.util.stream.Collectors;

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
				saveAndInsertImage(file, gdsNo, selUserNo, productsMapper.getMaxImageNo(), imgIndctSn++,
						ProductImageType.THUMBNAIL);
			}
		}
		if (request.getMainImage() != null) {
			for (MultipartFile file : request.getMainImage()) {
				saveAndInsertImage(file, gdsNo, selUserNo, productsMapper.getMaxImageNo(), imgIndctSn++,
						ProductImageType.MAIN);
			}
		}
		if (request.getDetailImage() != null) {
			for (MultipartFile file : request.getDetailImage()) {
				saveAndInsertImage(file, gdsNo, selUserNo, productsMapper.getMaxImageNo(), imgIndctSn++,
						ProductImageType.DETAIL);
			}
		}

		// 4. 옵션 정보 (PRODUCT_OPTION 및 PRODUCT_OPTION_VALUE) 생성 및 저장
		Map<String, String> genderOptionValueMap = new HashMap<>();
		Map<String, String> colorOptionValueMap = new HashMap<>();
		Map<String, String> sizeOptionValueMap = new HashMap<>();

		int optOrder = 1;
		if (request.getGenderOption() != null && !request.getGenderOption().isEmpty()) {
			String genderOptNo = productsMapper.getMaxOptionNo();
			genderOptionValueMap = insertOptionAndValue(gdsNo, selUserNo, "성별", "S", optOrder++, genderOptNo,
					List.of(request.getGenderOption()));
		}
		if (request.getColorOptions() != null && !request.getColorOptions().isEmpty()) {
			String colorOptNo = productsMapper.getMaxOptionNo();
			colorOptionValueMap = insertOptionAndValue(gdsNo, selUserNo, "색상", "S", optOrder++, colorOptNo,
					request.getColorOptions());
		}
		if (request.getSizeOptions() != null && !request.getSizeOptions().isEmpty()) {
			String sizeOptNo = productsMapper.getMaxOptionNo();
			sizeOptionValueMap = insertOptionAndValue(gdsNo, selUserNo, "사이즈", "S", optOrder++, sizeOptNo,
					request.getSizeOptions());
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

				System.out.println("CombinationData optVlNo1 (from frontend, assumed name): "
						+ combinationData.getOptVlNo1() + ", Mapped finalOptVlNo1: " + finalOptVlNo1);
				System.out.println("CombinationData optVlNo2 (from frontend, assumed name): "
						+ combinationData.getOptVlNo2() + ", Mapped finalOptVlNo2: " + finalOptVlNo2);
				System.out.println("CombinationData optVlNo3 (from frontend, assumed name): "
						+ combinationData.getOptVlNo3() + ", Mapped finalOptVlNo3: " + finalOptVlNo3);

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

	private void saveAndInsertImage(MultipartFile file, String gdsNo, String creatorNo, String imgNo, int imgIndctSn,
			ProductImageType imageType) {
		if (file.isEmpty())
			return;

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

	private Map<String, String> insertOptionAndValue(String gdsNo, String userNo, String optionName, String choiceType,
			int order, String optNo, List<String> values) {
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

	/**
	 * 카테고리 ID를 기반으로 해당 카테고리의 계층 정보를 조회합니다.
	 *
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
	public Map<String, Object> getFilteredAndSortedProducts(String categoryId, String sortBy,
			Map<String, Object> filterParams, int currentPage) {
		System.out.println("--- ProductsServiceImpl.getFilteredAndSortedProducts 진입 ---");
		System.out.println("전달받은 categoryId: " + categoryId);
		System.out.println("전달받은 filterParams: " + filterParams);

		Map<String, Object> paramMap = new HashMap<>(filterParams);
		paramMap.put("categoryId", categoryId);
		paramMap.put("sortBy", sortBy);
		
		// ⭐⭐⭐ 이 부분에서 discountRates 처리 로직을 추가합니다. ⭐⭐⭐
		if (filterParams.containsKey("discountRates")) {
		    Object discountRateObj = filterParams.get("discountRates");

		    // discountRates가 List 형태로 넘어오는 경우를 처리
		    if (discountRateObj instanceof List) {
		        List<?> discountRateList = (List<?>) discountRateObj;
		        if (!discountRateList.isEmpty()) {
		            // 리스트의 첫 번째 요소를 사용하거나, 특정 로직에 따라 처리
		            String discountRateStr = String.valueOf(discountRateList.get(0));
		            if (!"all".equalsIgnoreCase(discountRateStr)) {
		                paramMap.put("discountRate", discountRateStr); // 단일 값으로 전달
		            }
		        }
		    } else { // 단일 값으로 넘어오는 경우 (원래 예상했던 경우)
		        String discountRateStr = String.valueOf(discountRateObj);
		        if (!"all".equalsIgnoreCase(discountRateStr)) {
		            paramMap.put("discountRate", discountRateStr); // 단일 값으로 전달
		        }
		    }
		}
		// ⭐⭐⭐ discountRates 처리 로직 끝 ⭐⭐⭐

		System.out.println("매퍼에 전달될 최종 paramMap: " + paramMap);

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
		if (offset < 0)
			offset = 0; // 음수 방지

		// paramMap에 페이지네이션 정보 추가
		paramMap.put("pageSize", pageSize);
		paramMap.put("offset", offset);
		paramMap.put("currentPage", currentPage);

		// 2. 총 상품 개수 조회 (페이지네이션 계산에 필요)
		long totalProductsCount = productsMapper.countFilteredProducts(paramMap);

		// 3. 전체 페이지 수 계산
		int lastPage = (int) Math.ceil((double) totalProductsCount / pageSize);
		if (lastPage == 0)
			lastPage = 1; // 상품이 0개일 때 최소 1페이지

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

	@Override
	public long countFilteredProducts(String categoryId, String sortBy, Map<String, Object> filterParams) {
		Map<String, Object> paramMap = new HashMap<>(filterParams);
		paramMap.put("categoryId", categoryId);
		paramMap.put("sortBy", sortBy);
		
		// ⭐⭐⭐ 이 부분에서도 discountRates 처리 로직을 추가합니다. ⭐⭐⭐
		if (filterParams.containsKey("discountRates")) {
		    Object discountRateObj = filterParams.get("discountRates");

		    // discountRates가 List 형태로 넘어오는 경우를 처리
		    if (discountRateObj instanceof List) {
		        List<?> discountRateList = (List<?>) discountRateObj;
		        if (!discountRateList.isEmpty()) {
		            // 리스트의 첫 번째 요소를 사용하거나, 특정 로직에 따라 처리
		            String discountRateStr = String.valueOf(discountRateList.get(0));
		            if (!"all".equalsIgnoreCase(discountRateStr)) {
		                paramMap.put("discountRate", discountRateStr); // 단일 값으로 전달
		            }
		        }
		    } else { // 단일 값으로 넘어오는 경우 (원래 예상했던 경우)
		        String discountRateStr = String.valueOf(discountRateObj);
		        if (!"all".equalsIgnoreCase(discountRateStr)) {
		            paramMap.put("discountRate", discountRateStr); // 단일 값으로 전달
		        }
		    }
		}
		// ⭐⭐⭐ discountRates 처리 로직 끝 ⭐⭐⭐
		return productsMapper.countFilteredProducts(paramMap);
	}

	@Override
	public List<ProductOptionValue> getAllProductColors() {
		// productsMapper.getDistinctProductOptionValueNamesByType("색상") 호출 (vl_nm만 DISTINCT하게 가져옴)
		List<String> rawColorNames = productsMapper.getDistinctProductOptionValueNamesByType("색상");

		// Set을 사용하여 최종적으로 중복을 확실하게 제거하고 공백을 trim() 처리
		Set<String> uniqueColorNames = new HashSet<>();
		if (rawColorNames != null) {
			for (String colorName : rawColorNames) {
				uniqueColorNames.add(colorName.trim()); // 공백 제거
			}
		}

		// 정렬을 위해 다시 List로 변환 (알파벳/가나다 순으로 정렬)
		List<String> sortedUniqueColorNames = uniqueColorNames.stream()
															.sorted()
															.collect(Collectors.toList());

		// ProductOptionValue 객체 목록으로 변환 (여기서는 vl_nm만 중요하므로 새 객체 생성)
		List<ProductOptionValue> colorOptions = new ArrayList<>();
		for (String colorName : sortedUniqueColorNames) {
			ProductOptionValue pov = new ProductOptionValue();
			pov.setVlNm(colorName);
			// optVlNo나 다른 필드는 이 시점에서 필요 없으므로 설정하지 않음
			colorOptions.add(pov);
		}
		return colorOptions;
	}

    // ⭐⭐⭐ 이 부분부터 변경되었습니다. ⭐⭐⭐

    /**
     * 공통적으로 사용될 필터링, 중복 제거, 정렬 로직을 위한 헬퍼 메서드
     * 이 메서드는 특정 패턴에 맞는 사이즈 값을 추출하고, vl_nm 기준으로 중복을 제거하며, 정렬을 수행합니다.
     * 원본 ProductOptionValue 객체를 유지하여 vl_nm 외의 다른 필드(optVlNo 등)도 보존합니다.
     *
     * @param allSizes DB에서 가져온 모든 '사이즈' 옵션 값 리스트 (중복 포함 가능)
     * @param includePattern 이 패턴에 매칭되는 사이즈만 포함합니다. (null이면 패턴 필터링 없음)
     * @param excludePatterns 이 패턴들 중 하나라도 매칭되면 해당 사이즈는 제외합니다. (null 또는 비어있으면 제외 패턴 없음)
     * @param customOrder 특정 정렬 순서 (null일 경우 일반 정렬)
     * @return 필터링, 중복 제거, 정렬된 ProductOptionValue 리스트
     */
    private List<ProductOptionValue> processAndFilterAndSortSizes(
            List<ProductOptionValue> allSizes,
            Pattern includePattern,
            List<Pattern> excludePatterns, // 여러 개의 제외 패턴을 받을 수 있도록 List로 변경
            List<String> customOrder) {

        // vl_nm의 중복을 확인하기 위한 Set
        Set<String> seenVlNames = new HashSet<>();
        // 최종적으로 필터링되고 중복 제거된 ProductOptionValue 객체를 담을 리스트
        List<ProductOptionValue> filteredAndUnique = new ArrayList<>();

        for (ProductOptionValue pov : allSizes) {
            String trimmedVlNm = pov.getVlNm().trim(); // 앞뒤 공백 제거

            // 1. 포함 패턴 확인
            if (includePattern != null && !includePattern.matcher(trimmedVlNm).matches()) {
                continue; // 포함 패턴에 맞지 않으면 건너뛰기
            }

            // 2. 제외 패턴 확인
            boolean excluded = false;
            if (excludePatterns != null) {
                for (Pattern excludePattern : excludePatterns) {
                    if (excludePattern.matcher(trimmedVlNm).matches()) {
                        excluded = true;
                        break; // 하나라도 제외 패턴에 맞으면 제외
                    }
                }
            }
            if (excluded) {
                continue; // 제외 패턴에 맞으면 건너뛰기
            }

            // 3. vl_nm이 아직 Set에 없으면 (중복이 아니면) 추가하고 리스트에도 담음
            if (seenVlNames.add(trimmedVlNm)) {
                // ⭐ 원본 pov 객체를 그대로 추가하여 vl_nm 외의 다른 필드(예: optVlNo, clrCd)도 유지 ⭐
                filteredAndUnique.add(pov);
            }
        }

        // 정렬 로직
        filteredAndUnique.sort((s1, s2) -> {
            String s1Val = s1.getVlNm().trim(); // 대소문자 구분을 없애지 않고 원본 유지
            String s2Val = s2.getVlNm().trim(); // 대소문자 구분을 없애지 않고 원본 유지

            // 커스텀 정렬 순서가 있다면 적용 (예: XS, S, M, L)
            if (customOrder != null && !customOrder.isEmpty()) {
                // 커스텀 정렬 리스트는 대소문자를 통일해서 비교 (예: "FREE")
                int index1 = customOrder.indexOf(s1Val.toUpperCase());
                int index2 = customOrder.indexOf(s2Val.toUpperCase());

                if (index1 != -1 && index2 != -1) {
                    return Integer.compare(index1, index2);
                }
                // 커스텀 순서에 없는 항목은 자연스럽게 뒤로 가게 하거나, 다른 기준으로 정렬되도록 함
                if (index1 != -1) return -1; 
                if (index2 != -1) return 1;  
            }

            // 그 외는 숫자로 비교 (숫자로 변환 가능한 경우)
            try {
                // Double.parseDouble로 변경하여 "245.5"와 같은 소수점 숫자도 처리
                double num1 = Double.parseDouble(s1Val);
                double num2 = Double.parseDouble(s2Val);
                return Double.compare(num1, num2);
            } catch (NumberFormatException e) {
                // 숫자 파싱 실패 시 문자열 자체로 비교 (알파벳, 한글 등)
                return s1Val.compareTo(s2Val); // 원본 대소문자 유지하여 비교
            }
        });
        return filteredAndUnique;
    }


    @Override
    public List<ProductOptionValue> getAllApparelSizes() {
        // DB에서 '사이즈' 옵션 값을 모두 가져옵니다.
        List<ProductOptionValue> allSizes = productsMapper.getAllProductOptionValuesByType("사이즈");
        if (allSizes == null || allSizes.isEmpty()) {
            return List.of();
        }

        // 의류 사이즈 포함 패턴: XS, S, M, L, XL, 2XL과 같은 알파벳, 그리고 2~3자리 숫자 (예: 90, 100, 105).
        // 'FREE'와 '단일'은 여기서 제외되어야 합니다.
        Pattern apparelSizeIncludePattern = Pattern.compile(
            "^(XS|S|M|L|XL|2XL|\\d{2,3})$", 
            Pattern.CASE_INSENSITIVE
        );

        // 의류 사이즈에서 제외할 패턴: 신발 사이즈 패턴 (숫자, 소수점 포함)과 'FREE', '단일'
        Pattern shoeSizePatternForExclusion = Pattern.compile("^\\d{1,3}(\\.\\d)?$");
        Pattern freeOrSinglePattern = Pattern.compile("^(FREE|단일)$", Pattern.CASE_INSENSITIVE);
        
        List<Pattern> excludePatterns = new ArrayList<>();
        excludePatterns.add(shoeSizePatternForExclusion);
        excludePatterns.add(freeOrSinglePattern);

        // 의류 사이즈의 특정 정렬 순서 정의 (대소문자 무관하게 비교하기 위해 대문자로 정의)
        List<String> apparelOrder = List.of("XS", "S", "M", "L", "XL", "2XL");

        return processAndFilterAndSortSizes(allSizes, apparelSizeIncludePattern, excludePatterns, apparelOrder);
    }

    @Override
    public List<ProductOptionValue> getAllShoeSizes() {
        // DB에서 '사이즈' 옵션 값을 모두 가져옵니다.
        List<ProductOptionValue> allSizes = productsMapper.getAllProductOptionValuesByType("사이즈");
        if (allSizes == null || allSizes.isEmpty()) {
            return List.of();
        }

        // 신발 사이즈 포함 패턴: 1~3자리 숫자, 또는 소수점 한자리 숫자 (예: 230, 245.5).
        // 'FREE'와 '단일'은 여기서 제외되어야 합니다.
        Pattern shoeSizeIncludePattern = Pattern.compile("^\\d{1,3}(\\.\\d)?$");

        // 신발 사이즈에서 제외할 패턴: 의류 사이즈 패턴 (알파벳, 2~3자리 숫자)과 'FREE', '단일'
        Pattern apparelSizePatternForExclusion = Pattern.compile(
            "^(XS|S|M|L|XL|2XL|\\d{2,3})$", 
            Pattern.CASE_INSENSITIVE
        );
        Pattern freeOrSinglePattern = Pattern.compile("^(FREE|단일)$", Pattern.CASE_INSENSITIVE);

        List<Pattern> excludePatterns = new ArrayList<>();
        excludePatterns.add(apparelSizePatternForExclusion);
        excludePatterns.add(freeOrSinglePattern);

        // 신발 사이즈는 숫자로만 정렬하므로 커스텀 순서 없음 (null 전달)
        return processAndFilterAndSortSizes(allSizes, shoeSizeIncludePattern, excludePatterns, null);
    }

    @Override
    public List<ProductOptionValue> getAllFashionSizes() {
        // DB에서 '사이즈' 옵션 값을 모두 가져옵니다.
        List<ProductOptionValue> allSizes = productsMapper.getAllProductOptionValuesByType("사이즈");
        if (allSizes == null || allSizes.isEmpty()) {
            return List.of();
        }

        // ⭐ 패션 소품 사이즈 포함 패턴: 'FREE' 또는 '단일'만 포함하도록 명확히 정의합니다. ⭐
        // 대소문자 구분 없이 'FREE' 또는 '단일'만 포함합니다.
        Pattern fashionSizeIncludePattern = Pattern.compile(
            "^(FREE|단일)$",
            Pattern.CASE_INSENSITIVE
        );

        // 패션 소품 사이즈는 위 포함 패턴에 맞는 것만 가져오므로, 제외 패턴은 필요 없습니다. (null 전달)
        // customOrder는 'FREE', '단일' 순서로 정렬합니다 (대소문자 무관).
        List<String> fashionOrder = List.of("FREE", "단일");

        return processAndFilterAndSortSizes(allSizes, fashionSizeIncludePattern, null, fashionOrder);
    }
    // ⭐⭐⭐ 변경된 부분 끝 ⭐⭐⭐


	@Override
	public List<Stores> getAllBrands() {
		return productsMapper.getAllStores();
	}
}