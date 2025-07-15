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
		product.setCtgryNo(StringUtils.hasText(request.getProductCategory2()) ? request.getProductCategory2()
				: request.getProductCategory1());
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
		// ========================> 디버깅 로그 추가 <========================
        log.info("============== [Service Impl] DTO Check ==============");
        if (request.getDeletedImageIds() != null) {
            log.info("request.getDeletedImageIds()의 타입: {}", request.getDeletedImageIds().getClass().getName());
            log.info("request.getDeletedImageIds()의 내용: {}", request.getDeletedImageIds());
        } else {
            log.info("request.getDeletedImageIds()가 null입니다.");
        }
        log.info("======================================================");
        // ====================================================================
		String gdsNo = request.getGdsNo();
		if (!StringUtils.hasText(gdsNo)) {
			throw new IllegalArgumentException("상품 수정을 위한 상품 ID가 없습니다.");
		}

		String selUserNo = request.getSelUserNo();

		// 1. 상품 기본 정보 업데이트
		Products product = new Products();
		product.setGdsNo(gdsNo);
		product.setCtgryNo(StringUtils.hasText(request.getProductCategory2()) ? request.getProductCategory2()
				: request.getProductCategory1());
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

		// 2. 기존 연관 데이터 삭제 (순서 중요!)
		// 2-1. 상태-옵션 매핑 먼저 삭제
		productsMapper.deleteStatusOptionMappingsByGdsNo(gdsNo);
		// 2-2. 옵션 값 삭제
		productsMapper.deleteOptionValuesByGdsNo(gdsNo);
		// 2-3. 옵션 삭제
		productsMapper.deleteOptionsByGdsNo(gdsNo);
		// 2-4. 상태 삭제
		productsMapper.deleteStatusByGdsNo(gdsNo);

		// 2-5. 이미지 처리: '삭제할 이미지 ID 목록'을 이용해 명확하게 삭제
		// 사용자가 X 버튼을 눌러 지정한 이미지들만 삭제합니다.
		if (request.getDeletedImageIds() != null && !request.getDeletedImageIds().isEmpty()) {
			// 여기에 물리적 파일 삭제 로직을 추가하면 더 좋습니다. (선택사항)
			// List<ProductImage> imagesToDelete =
			// productsMapper.getImagesByImageNos(request.getDeletedImageIds());
			// for(ProductImage img : imagesToDelete) {
			// filesUtils.deleteFile(img.getImgFilePathNm());
			// }

			// DB에서 이미지 정보 삭제
			productsMapper.deleteImagesByImageNos(request.getDeletedImageIds());
		}
		
		// ⭐⭐⭐⭐⭐ [3단계: 순서 업데이트 로직 추가] 시작 ⭐⭐⭐⭐⭐
	    // (B) 기존 이미지 순서 업데이트
	    int nextSortKey = 1; // 이미지 순번(img_indct_sn)은 1부터 다시 시작
	    
	    boolean isNewThumbnailUploaded = request.getThumbnailImage() != null && !request.getThumbnailImage().isEmpty() && !request.getThumbnailImage().get(0).isEmpty();
	    if (isNewThumbnailUploaded) {
	        // 새 썸네일이 업로드되면, 기존 썸네일은 무조건 삭제합니다. (순서 업데이트 불필요)
	        productsMapper.deleteImagesByGdsNoAndType(gdsNo, ProductImageType.THUMBNAIL);
	    } else {
	        // 새 썸네일이 없다면, 기존 썸네일의 순서만 1로 업데이트합니다.
	        List<ProductImage> thumbs = productsMapper.getProductImagesByGdsNoAndType(gdsNo, ProductImageType.THUMBNAIL);
	        if (thumbs != null && !thumbs.isEmpty() && thumbs.get(0) != null) {
	            productsMapper.updateImageOrder(thumbs.get(0).getImgNo(), nextSortKey++);
	        }
	    }
	    // 대표 이미지 순서 업데이트
	    if (request.getImageOrderMain() != null) {
	        for (String imgNo : request.getImageOrderMain()) {
	            productsMapper.updateImageOrder(imgNo, nextSortKey++);
	        }
	    }
	    
	    // 상세 이미지 순서 업데이트
	    if (request.getImageOrderDetail() != null) {
	        for (String imgNo : request.getImageOrderDetail()) {
	            productsMapper.updateImageOrder(imgNo, nextSortKey++);
	        }
	    }
	    // ⭐⭐⭐⭐⭐ [3단계: 로직 추가] 끝 ⭐⭐⭐⭐⭐
		
		// 3. 새로 추가된 파일들만 저장
		saveProductImages(request, gdsNo);
		saveOptionsAndStock(request, gdsNo);

		// 4. 재승인 절차
		int newCycle = adminProductManagementMapper.getLatestApprovalCycle(gdsNo) + 1;
		String newHistoryCode = productsMapper.getMaxApprovalHistoryCode();
		ProductApprovalHistory history = new ProductApprovalHistory();
		history.setAprvRjctHstryCd(newHistoryCode);
		history.setGdsNo(gdsNo);
		history.setPrcsMngrId(selUserNo);
		history.setAprvSttsCd("대기");
		history.setPrcsDt(LocalDateTime.now());
		history.setAprvRjctCycl(newCycle);
		history.setMngrCmntCn("판매자 수정 요청으로 인한 재승인 대기 중");
		adminProductManagementMapper.insertProductApprovalHistory(history);

		// 5. 상품 노출 상태를 false로 변경
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

		if (allSizes == null)
			return new ArrayList<>();

		// vlNm을 기준으로 중복을 제거하고, 정해진 순서대로 정렬합니다.
		return allSizes.stream()
				// 1. vlNm이 null이 아니고, 의류 사이즈 목록에 포함된 값만 필터링
				.filter(pov -> pov.getVlNm() != null && apparelSizeOrder.contains(pov.getVlNm().toUpperCase()))
				// 2. vlNm 기준으로 중복 제거 (Map을 활용)
				.collect(Collectors.toMap(pov -> pov.getVlNm().toUpperCase(), // Key: 사이즈 이름 (대소문자 무관)
						pov -> pov, // Value: 첫 번째로 발견된 객체
						(existing, replacement) -> existing // 중복 키 발생 시 기존 값 유지
				)).values().stream() // 3. 중복 제거된 Map의 값들(ProductOptionValue 객체)만 다시 스트림으로
				// 4. 정해진 순서대로 정렬
				.sorted(Comparator.comparingInt(pov -> apparelSizeOrder.indexOf(pov.getVlNm().toUpperCase())))
				.collect(Collectors.toList());
	}

	// 필터용 신발 사이즈 목록 조회
	@Override
	public List<ProductOptionValue> getAllShoeSizes() {
		List<ProductOptionValue> allSizes = productOptionMapper.getAllProductOptionValuesByType("사이즈");

		if (allSizes == null)
			return new ArrayList<>();

		return allSizes.stream().filter(pov -> pov.getVlNm() != null && pov.getVlNm().matches("^[0-9]+(\\.[0-9])?$"))
				.collect(Collectors.toMap(ProductOptionValue::getVlNm, pov -> pov, (existing, replacement) -> existing))
				.values().stream().sorted(Comparator.comparingDouble(pov -> Double.parseDouble(pov.getVlNm())))
				.collect(Collectors.toList());
	}

	// 필터용 패션잡화 사이즈 목록 조회
	@Override
	public List<ProductOptionValue> getAllFashionSizes() {
		List<ProductOptionValue> allSizes = productOptionMapper.getAllProductOptionValuesByType("사이즈");
		List<String> fashionSizeOrder = List.of("FREE", "단일");

		if (allSizes == null)
			return new ArrayList<>();

		return allSizes.stream()
				.filter(pov -> pov.getVlNm() != null && fashionSizeOrder.contains(pov.getVlNm().toUpperCase()))
				.collect(Collectors.toMap(pov -> pov.getVlNm().toUpperCase(), pov -> pov,
						(existing, replacement) -> existing))
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
		imgIndctSn = saveAndRegisterImage(request.getThumbnailImage(), gdsNo, request.getSelUserNo(), imgIndctSn,
				ProductImageType.THUMBNAIL);
		imgIndctSn = saveAndRegisterImage(request.getMainImage(), gdsNo, request.getSelUserNo(), imgIndctSn,
				ProductImageType.MAIN);
		saveAndRegisterImage(request.getDetailImage(), gdsNo, request.getSelUserNo(), imgIndctSn,
				ProductImageType.DETAIL);
	}

	private int saveAndRegisterImage(List<MultipartFile> files, String gdsNo, String creatorNo, int startSn,
	        ProductImageType imageType) {
	    // 1. files 리스트가 null이거나 비어있으면 즉시 반환
	    if (files == null || files.isEmpty()) {
	        return startSn;
	    }

	    int currentSn = startSn;

	    for (MultipartFile file : files) {
	        // 2. ⭐⭐⭐[핵심 수정]⭐⭐⭐
	        //    List 안에 실제 파일 데이터가 있는지 한번 더 확인합니다.
	        //    Spring이 빈 file input을 ""(빈 문자열)이 아닌,
	        //    내용이 없는 MultipartFile 객체로 넘겨줄 수 있기 때문입니다.
	        if (file == null || file.isEmpty()) {
	            continue; // 파일이 비어있으면 다음 루프로 넘어갑니다.
	        }

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
	    return currentSn;
	}

	private void saveOptionsAndStock(ProductRegistrationRequest request, String gdsNo) {
		Map<String, String> genderOptionValueMap = new HashMap<>();
		Map<String, String> colorOptionValueMap = new HashMap<>();
		Map<String, String> sizeOptionValueMap = new HashMap<>();
		String creatorNo = request.getSelUserNo();

		int optOrder = 1;

		// 1. 성별 옵션 처리 (변경 없음)
		if (StringUtils.hasText(request.getGenderOption())) {
			genderOptionValueMap = insertOptionAndValue(gdsNo, creatorNo, "성별", "S", optOrder++,
					List.of(request.getGenderOption()));
		}

		// 2. 색상 옵션 처리 - 수정 모드에서는 productOptionCombinations에서 색상 추출
		List<String> colorOptionsToSave = new ArrayList<>();
		if (request.getProductOptionCombinations() != null && !request.getProductOptionCombinations().isEmpty()) {
			colorOptionsToSave = request.getProductOptionCombinations().stream()
					.map(ProductCombinationData::getOptVlNo2).filter(StringUtils::hasText).distinct()
					.collect(Collectors.toList());
		} else if (request.getColorOptions() != null) { // 등록 모드일 경우
			colorOptionsToSave = request.getColorOptions().stream().distinct().collect(Collectors.toList());
		}

		if (!colorOptionsToSave.isEmpty()) {
			colorOptionValueMap = insertOptionAndValue(gdsNo, creatorNo, "색상", "S", optOrder++, colorOptionsToSave);
		}

		// 3. 사이즈 옵션 처리 - 색상과 동일한 방식
		List<String> sizeOptionsToSave = new ArrayList<>();
		if (request.getProductOptionCombinations() != null && !request.getProductOptionCombinations().isEmpty()) {
			sizeOptionsToSave = request.getProductOptionCombinations().stream().map(ProductCombinationData::getOptVlNo3)
					.filter(StringUtils::hasText).distinct().collect(Collectors.toList());
		} else if (request.getSizeOptions() != null) { // 등록 모드일 경우
			sizeOptionsToSave = request.getSizeOptions().stream().distinct().collect(Collectors.toList());
		}

		if (!sizeOptionsToSave.isEmpty()) {
			sizeOptionValueMap = insertOptionAndValue(gdsNo, creatorNo, "사이즈", "S", optOrder++, sizeOptionsToSave);
		}

		// 4. 재고 정보 처리 (기존 로직 유지)
		if (request.getProductOptionCombinations() != null && !request.getProductOptionCombinations().isEmpty()) {
			for (ProductCombinationData combinationData : request.getProductOptionCombinations()) {
				String gdsSttsNo = productsMapper.getMaxStatusNo();
				insertProductStatus(gdsSttsNo, gdsNo, creatorNo, combinationData.getQuantity());

				String genderOptValueId = genderOptionValueMap.get(combinationData.getOptVlNo1());
				String colorOptValueId = colorOptionValueMap.get(combinationData.getOptVlNo2());
				String sizeOptValueId = sizeOptionValueMap.get(combinationData.getOptVlNo3());

				if (StringUtils.hasText(genderOptValueId))
					insertStatusOptionMapping(gdsSttsNo, genderOptValueId, creatorNo);
				if (StringUtils.hasText(colorOptValueId))
					insertStatusOptionMapping(gdsSttsNo, colorOptValueId, creatorNo);
				if (StringUtils.hasText(sizeOptValueId))
					insertStatusOptionMapping(gdsSttsNo, sizeOptValueId, creatorNo);
			}
		} else {
			String gdsSttsNo = productsMapper.getMaxStatusNo();
			insertProductStatus(gdsSttsNo, gdsNo, creatorNo, request.getStockQuantity());
		}
	}

	private Map<String, String> insertOptionAndValue(String gdsNo, String creatorNo, String optionName,
			String choiceType, int order, List<String> values) {
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