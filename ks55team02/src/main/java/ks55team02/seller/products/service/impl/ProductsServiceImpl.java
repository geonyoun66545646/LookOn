package ks55team02.seller.products.service.impl;

// 필요한 모든 도메인/DTO 클래스 임포트
import ks55team02.seller.products.domain.*;
import ks55team02.seller.products.domain.ProductRegistrationRequest.ProductCombinationData; // 내부 클래스 명시적 임포트
import ks55team02.seller.products.mapper.ProductsMapper;
import ks55team02.seller.products.service.ProductsService; // 서비스 인터페이스 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils; // 유틸리티 함수 임포트

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime; // 날짜/시간 객체 임포트
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service // 이 클래스가 서비스 계층의 컴포넌트임을 나타냅니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성 주입 (Lombok)
public class ProductsServiceImpl implements ProductsService {
    private final ProductsMapper productsMapper; // MyBatis Mapper 의존성 주입

    // application.properties 또는 application.yml에 정의된 파일 업로드 경로를 주입받습니다.
    @Value("${product.upload.dir}")
    private String uploadDir;

    /**
     * 판매자 번호(selUserNo)와 스토어 ID(storeId)에 해당하는 상품 목록을 조회합니다.
     *
     * @param selUserNo 판매자 번호
     * @param storeId 스토어 ID
     * @return 조건에 맞는 상품 목록 (List<Products>)
     */
    @Override
    public List<Products> getProductsBySellerAndStore(String selUserNo, String storeId) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("selUserNo", selUserNo);
        paramMap.put("storeId", storeId);
        return productsMapper.getProductsBySellerAndStore(paramMap);
    }

    /**
     * 새로운 상품을 등록하고 관련 이미지, 옵션, 옵션 조합별 재고 정보를 처리합니다.
     * 이 메서드 내에서 여러 Mapper 메서드를 호출하며, 전체 작업이 하나의 트랜잭션으로 관리됩니다.
     * 도중에 예외 발생 시 모든 변경 사항은 롤백됩니다.
     *
     * @param request 상품 등록에 필요한 모든 정보를 담은 요청 객체 (ProductRegistrationRequest DTO)
     */
    @Override
    @Transactional // 트랜잭션 관리 어노테이션: 메서드 실행 중 오류 발생 시 모든 DB 작업 롤백
    public void addProduct(ProductRegistrationRequest request) {
        String selUserNo = request.getSelUserNo(); // 판매자 번호
        String storeId = request.getStoreId(); // 스토어 ID

        // 1. 상품 번호(gdsNo) 생성
        // productsMapper.getMaxProductCode()는 DB에서 다음 상품 번호를 가져오는 매퍼 메서드입니다.
        // 예를 들어, "GDS00001", "GDS00002"와 같은 형태의 고유한 상품 번호를 생성합니다.
        // 만약 UUID 형태의 완전한 고유값을 원한다면 `UUID.randomUUID().toString()`을 사용할 수도 있습니다.
        String gdsNo = productsMapper.getMaxProductCode();
        // ProductRegistrationRequest DTO에 `gdsNo` 필드를 설정합니다.
        // 이 `gdsNo`는 이후 상품 이미지, 옵션, 재고 상태 등을 연결하는 외래 키로 사용됩니다.
        request.setGdsNo(gdsNo);

        // 2. 기본 상품 정보 설정 및 PRODUCTS 테이블에 저장
        Products product = new Products();
        product.setGdsNo(gdsNo); // 생성된 상품 번호 설정
        product.setStoreId(storeId); // 스토어 ID 설정

        // 카테고리 설정 로직: 2차 카테고리가 있으면 2차를, 없으면 1차를 사용합니다.
        String finalCategoryNo = null;
        if (StringUtils.hasText(request.getProductCategory2())) {
            finalCategoryNo = request.getProductCategory2();
        } else if (StringUtils.hasText(request.getProductCategory1())) {
            finalCategoryNo = request.getProductCategory1();
        }
        // 최종 카테고리 번호가 유효하지 않으면 예외 발생 (클라이언트 측 유효성 검사가 먼저 수행되어야 함)
        if (!StringUtils.hasText(finalCategoryNo)) {
            throw new IllegalArgumentException("상품 카테고리가 유효하지 않습니다. 1차 또는 2차 카테고리를 선택해주세요.");
        }
        product.setCtgryNo(finalCategoryNo); // 최종 카테고리 번호 설정

        product.setSelUserNo(selUserNo); // 판매자 번호 설정
        product.setMdfrNo(selUserNo); // 수정자 번호는 등록자와 동일하게 설정
        product.setGdsNm(request.getProductName()); // 상품명
        product.setGdsExpln(request.getProductDescription()); // 상품 설명
        product.setBasPrc(request.getBasePrice() != null ? request.getBasePrice().intValue() : 0); // 기본 가격
        product.setDscntRt(request.getDiscountRate()); // 할인율

        // 최소/최대 구매 수량 설정
        product.setMinPurchaseQty(request.getMinPurchase());
        product.setMaxPurchaseQty(request.getMaxPurchase());
        
        // 최종 판매 가격 계산 (ProductRegistrationRequest DTO 내부의 편의 메서드 활용)
        request.calculateFinalPrice();
        product.setLastSelPrc(request.getFinalPrice() != null ? request.getFinalPrice().intValue() : 0); // 최종 판매 가격

        product.setRegDt(LocalDateTime.now()); // 등록일시
        product.setExpsrYn(true); // 노출 여부 (기본: 노출)
        product.setActvtnYn(true); // 활성화 여부 (기본: 활성화)

        productsMapper.addProduct(product); // PRODUCTS 테이블에 상품 기본 정보 삽입

        // 3. 이미지 업로드 및 PRODUCT_IMAGE 테이블에 DB 저장
        int imgIndctSn = 1; // 이미지 표시 순서 카운터

        // 썸네일 이미지 처리
        if (request.getThumbnailImage() != null) {
            for (MultipartFile file : request.getThumbnailImage()) {
                // 헬퍼 메서드를 호출하여 파일 저장 및 DB에 이미지 정보 삽입
                saveAndInsertImage(file, gdsNo, selUserNo, productsMapper.getMaxImageNo(), imgIndctSn++, ProductImageType.THUMBNAIL);
            }
        }
        // 메인 이미지 처리
        if (request.getMainImage() != null) {
            for (MultipartFile file : request.getMainImage()) {
                saveAndInsertImage(file, gdsNo, selUserNo, productsMapper.getMaxImageNo(), imgIndctSn++, ProductImageType.MAIN);
            }
        }
        // 상세 이미지 처리
        if (request.getDetailImage() != null) {
            for (MultipartFile file : request.getDetailImage()) {
                saveAndInsertImage(file, gdsNo, selUserNo, productsMapper.getMaxImageNo(), imgIndctSn++, ProductImageType.DETAIL);
            }
        }

        // 4. 옵션 정보 (PRODUCT_OPTION 및 PRODUCT_OPTION_VALUE) 생성 및 저장
        // 각 옵션 값 이름(e.g., "남성", "빨강")과 실제 DB에 삽입된 옵션 값 번호(optVlNo)를 매핑하는 맵을 저장합니다.
        Map<String, String> genderOptionValueMap = new HashMap<>();
        Map<String, String> colorOptionValueMap = new HashMap<>();
        Map<String, String> sizeOptionValueMap = new HashMap<>();

        int optOrder = 1; // 옵션 표시 순서 카운터
        // 성별 옵션 처리
        if (request.getGenderOption() != null && !request.getGenderOption().isEmpty()) {
            String genderOptNo = productsMapper.getMaxOptionNo(); // 새로운 옵션 번호 생성
            // genderOption은 단일 문자열이므로 List.of()로 감싸서 전달
            genderOptionValueMap = insertOptionAndValue(gdsNo, selUserNo, "성별", "S", optOrder++, genderOptNo, List.of(request.getGenderOption()));
        }
        // 색상 옵션 처리
        if (request.getColorOptions() != null && !request.getColorOptions().isEmpty()) {
            String colorOptNo = productsMapper.getMaxOptionNo();
            colorOptionValueMap = insertOptionAndValue(gdsNo, selUserNo, "색상", "S", optOrder++, colorOptNo, request.getColorOptions());
        }
        // 사이즈 옵션 처리
        if (request.getSizeOptions() != null && !request.getSizeOptions().isEmpty()) {
            String sizeOptNo = productsMapper.getMaxOptionNo();
            sizeOptionValueMap = insertOptionAndValue(gdsNo, selUserNo, "사이즈", "S", optOrder++, sizeOptNo, request.getSizeOptions());
        }

        // ⭐⭐⭐ 5. 옵션 조합별 재고 수량 및 매핑 정보 저장 ⭐⭐⭐
        // `ProductRegistrationRequest` DTO의 `productOptionCombinations` 리스트를 처리합니다.
        // 이 리스트의 각 요소(ProductCombinationData)는 프론트엔드에서 넘어온 특정 옵션 조합의 데이터입니다.
        if (request.getProductOptionCombinations() != null && !request.getProductOptionCombinations().isEmpty()) {
            for (ProductCombinationData combinationData : request.getProductOptionCombinations()) {
                // 각 옵션 조합에 대해 `PRODUCT_STATUS` 테이블에 새로운 레코드를 생성합니다.
                // `PRODUCT_STATUS`는 이 특정 옵션 조합에 대한 재고 상태를 나타냅니다.
                String gdsSttsNo = productsMapper.getMaxStatusNo(); // 새로운 상품 상태 번호 생성

                ProductStatus productStatus = new ProductStatus();
                productStatus.setGdsSttsNo(gdsSttsNo); // 생성된 상품 상태 번호 설정 (PK)
                productStatus.setGdsNo(gdsNo); // 부모 상품의 ID (위에서 생성된 상품 번호) 연결
                productStatus.setCreatrNo(selUserNo); // 생성자 번호 (판매자 번호)
                productStatus.setMdfrNo(selUserNo); // 수정자 번호 (등록 시 생성자와 동일)
                productStatus.setSelPsbltyQntty(combinationData.getQuantity()); // 해당 조합의 판매 가능 수량
                productStatus.setRegDt(LocalDateTime.now()); // 등록일시
                productStatus.setSldoutYn(combinationData.getQuantity() <= 0); // 수량이 0 이하면 품절
                productStatus.setActvtnYn(true); // 활성화 여부 (기본: 활성화)

                productsMapper.insertProductStatus(productStatus); // `PRODUCT_STATUS` 테이블에 데이터 삽입

                // `ProductCombinationData`의 `optVlNo1`, `optVlNo2`, `optVlNo3` 필드가
                // 실제로는 옵션 값의 *이름* (예: "남성", "빨강", "M")을 담고 있다고 가정하고,
                // 이를 사용하여 위에서 생성된 `optVlNo` (옵션 값 번호)를 찾아 매핑합니다.
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

                // **디버깅을 위해 이 부분에 로그를 추가하여 actual opt_vl_no가 올바르게 매핑되는지 확인하세요.**
                System.out.println("CombinationData optVlNo1 (from frontend, assumed name): " + combinationData.getOptVlNo1() + ", Mapped finalOptVlNo1: " + finalOptVlNo1);
                System.out.println("CombinationData optVlNo2 (from frontend, assumed name): " + combinationData.getOptVlNo2() + ", Mapped finalOptVlNo2: " + finalOptVlNo2);
                System.out.println("CombinationData optVlNo3 (from frontend, assumed name): " + combinationData.getOptVlNo3() + ", Mapped finalOptVlNo3: " + finalOptVlNo3);


                if (StringUtils.hasText(finalOptVlNo1)) {
                    StatusOptionMapping mapping1 = new StatusOptionMapping();
                    mapping1.setGdsSttsNo(gdsSttsNo);
                    mapping1.setOptVlNo(finalOptVlNo1); // DB에 저장된 실제 opt_vl_no 사용
                    mapping1.setCreatrNo(selUserNo);
                    mapping1.setDelUserNo(null); // 초기 등록 시 삭제자 번호는 null
                    mapping1.setCrtYmd(LocalDateTime.now()); // 생성일
                    mapping1.setInactvtnDt(null); // 초기 등록 시 비활성화 일시는 null
                    mapping1.setActvtnYn(true); // 활성화
                    productsMapper.insertStatusOptionMapping(mapping1);
                }
                if (StringUtils.hasText(finalOptVlNo2)) {
                    StatusOptionMapping mapping2 = new StatusOptionMapping();
                    mapping2.setGdsSttsNo(gdsSttsNo);
                    mapping2.setOptVlNo(finalOptVlNo2); // DB에 저장된 실제 opt_vl_no 사용
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
                    mapping3.setOptVlNo(finalOptVlNo3); // DB에 저장된 실제 opt_vl_no 사용
                    mapping3.setCreatrNo(selUserNo);
                    mapping3.setDelUserNo(null);
                    mapping3.setCrtYmd(LocalDateTime.now());
                    mapping3.setInactvtnDt(null);
                    mapping3.setActvtnYn(true);
                    productsMapper.insertStatusOptionMapping(mapping3);
                }
            }
        } else {
             // 만약 옵션 조합이 하나도 넘어오지 않은 경우 (예: 옵션 없는 단일 상품),
             // `ProductRegistrationRequest`의 `stockQuantity` 필드에 있는 총 재고 수량을 기반으로
             // 단일 `ProductStatus` 레코드를 생성하여 저장합니다.
            String gdsSttsNo = productsMapper.getMaxStatusNo(); // 새로운 상품 상태 번호 생성
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

        // 6. 상품 태그 및 동영상 URL 저장 (필요 시 구현)
        // 현재는 DTO에 필드만 있고 저장 로직은 없습니다. 필요에 따라 매퍼 호출을 추가하세요.
        // if (StringUtils.hasText(request.getProductTags())) { /* 태그 저장 로직 */ }
        // if (StringUtils.hasText(request.getVideoUrl())) { /* 동영상 URL 저장 로직 */ }
    }

    /**
     * 이미지 파일을 지정된 경로에 저장하고, 해당 이미지 정보를 DB(`PRODUCT_IMAGE` 테이블)에 삽입하는 헬퍼 메서드입니다.
     *
     * @param file 멀티파트 파일 객체 (업로드된 이미지 파일)
     * @param gdsNo 상품 번호 (이미지가 속한 상품)
     * @param creatorNo 생성자(판매자) 번호
     * @param imgNo 이미지 번호 (DB에서 새로 생성된 고유 번호)
     * @param imgIndctSn 이미지 표시 순서
     * @param imageType 이미지 유형 (예: THUMBNAIL, MAIN, DETAIL)
     */
    private void saveAndInsertImage(MultipartFile file, String gdsNo, String creatorNo, String imgNo, int imgIndctSn, ProductImageType imageType) {
        if (file.isEmpty()) return; // 파일이 비어있으면 더 이상 처리하지 않고 반환

        Path filePath = null; // 저장될 파일의 전체 경로를 저장할 변수

        try {
            // 업로드 디렉토리 생성: 지정된 경로에 디렉토리가 없으면 생성합니다.
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 파일 확장자 추출
            String ext = "";
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            // 저장될 파일 이름: 이미지 번호 + 추출된 확장자 (예: IMG00001.png)
            String savedFileName = imgNo + ext;
            filePath = uploadPath.resolve(savedFileName); // 업로드 디렉토리와 파일 이름을 결합하여 최종 경로 생성

            // 파일 복사: 업로드된 파일을 지정된 경로에 복사합니다.
            // `StandardCopyOption.REPLACE_EXISTING`은 같은 이름의 파일이 이미 존재하면 덮어쓰도록 합니다.
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // `ProductImage` DTO 객체 생성 및 정보 설정
            ProductImage productImage = new ProductImage();
            productImage.setImgNo(imgNo); // 생성된 이미지 번호 설정
            productImage.setGdsNo(gdsNo); // 연결될 상품 번호 설정
            productImage.setCreatrNo(creatorNo); // 생성자(판매자) 번호
            productImage.setMdfrNo(creatorNo); // 수정자 번호는 생성자와 동일하게 설정
            // 웹에서 접근 가능한 이미지 파일 경로 설정 (클라이언트에서 이미지를 로드할 때 사용될 경로)
            // `/maincss/assets/images/sellerUploads/`는 웹 서버의 정적 자원 매핑 경로와 일치해야 합니다.
            productImage.setImgFilePathNm("/maincss/assets/images/sellerUploads/" + savedFileName);
            productImage.setImgIndctSn(imgIndctSn); // 이미지 표시 순서
            productImage.setRegDt(LocalDateTime.now()); // 등록일시
            productImage.setImgType(imageType); // 이미지 유형 (썸네일, 메인, 상세 등)
            productImage.setActvtnYn(true); // 활성화 여부 (기본: 활성화)

            productsMapper.insertProductImage(productImage); // `PRODUCT_IMAGE` 테이블에 이미지 정보 삽입

        } catch (IOException e) {
            // 이미지 처리 중 I/O 오류(파일 읽기/쓰기 문제) 발생 시 런타임 예외 발생
            String errorFilePath = (filePath != null) ? filePath.toString() : "알 수 없는 파일 경로";
            throw new RuntimeException("이미지 처리 중 오류 발생: " + errorFilePath, e);
        }
    }

    /**
     * 상품 옵션(`PRODUCT_OPTION` 테이블)과 해당 옵션의 값들(`PRODUCT_OPTION_VALUE` 테이블)을 DB에 삽입하는 헬퍼 메서드입니다.
     * 이 함수는 새로운 옵션(예: "성별")과 그에 대한 새로운 옵션 값들(예: "남성", "여성")을 등록하는 데 사용됩니다.
     *
     * @param gdsNo 상품 번호 (옵션이 속한 상품)
     * @param userNo 생성자(판매자) 번호
     * @param optionName 옵션 이름 (예: "색상", "사이즈", "성별")
     * @param choiceType 선택 유형 코드 (예: "S": 단일 선택, "M": 복수 선택)
     * @param order 옵션 표시 순서
     * @param optNo 생성된 옵션 번호 (DB에서 부여된 고유 번호)
     * @param values 옵션 값 이름 목록 (예: ["빨강", "파랑"], ["S", "M", "L"])
     * @return `Map<String, String>` - 옵션 값 이름(key)과 해당 옵션 값 번호(value)의 매핑
     */
    private Map<String, String> insertOptionAndValue(String gdsNo, String userNo, String optionName, String choiceType, int order, String optNo, List<String> values) {
        Map<String, String> generatedOptionValueIds = new HashMap<>(); // To store vlNm -> optVlNo mapping

        // 1. `ProductOption` DTO 객체 생성 및 정보 설정 후 `PRODUCT_OPTION` 테이블에 삽입
        ProductOption option = new ProductOption();
        option.setOptNo(optNo); // 생성된 옵션 번호
        option.setGdsNo(gdsNo); // 연결될 상품 번호
        option.setCreatrNo(userNo); // 생성자(판매자) 번호
        option.setMdfrNo(userNo); // 수정자 번호
        option.setOptNm(optionName); // 옵션명
        option.setSnglMtplChcSeCd(choiceType); // 단일/복수 선택 구분 코드
        option.setOptIndctSn(order); // 옵션 표시 순서
        option.setRegDt(LocalDateTime.now()); // 등록일시
        option.setActvtnYn(true); // 활성화 여부

        productsMapper.insertProductOption(option); // `PRODUCT_OPTION` 테이블에 옵션 정보 삽입

        // 2. `ProductOptionValue` DTO 객체 생성 및 정보 설정 후 `PRODUCT_OPTION_VALUE` 테이블에 삽입
        for (String val : values) {
            ProductOptionValue value = new ProductOptionValue();
            String optVlNo = productsMapper.getMaxOptionValueNo(); // 새로운 옵션 값 번호 생성
            value.setOptVlNo(optVlNo);
            value.setOptNo(optNo); // ✅ 이 부분이 중요: 방금 생성한 `optNo`와 이 옵션 값(`vlNm`)을 연결합니다.
            value.setCreatrNo(userNo);
            value.setMdfrNo(userNo);
            value.setVlNm(val); // 옵션 값 이름 (예: "빨강", "M")
            value.setRegDt(LocalDateTime.now());
            value.setActvtnYn(true);

            productsMapper.insertProductOptionValue(value); // `PRODUCT_OPTION_VALUE` 테이블에 옵션 값 정보 삽입

            // 생성된 옵션 값 번호를 맵에 저장
            generatedOptionValueIds.put(val, optVlNo);
        }
        return generatedOptionValueIds; // 생성된 옵션 값 ID 맵 반환
    }

    /**
     * 특정 상품 코드가 데이터베이스에 이미 존재하는지 확인합니다.
     *
     * @param productCode 확인할 상품 코드
     * @return true면 중복, false면 중복 아님
     */
    @Override
    public boolean isProductCodeDuplicated(String productCode) {
        return productsMapper.countProductCode(productCode) > 0;
    }

    /**
     * 판매자/관리자용: 모든 상품 목록을 조회합니다. (필요 시 사용)
     * 이 메서드는 현재 `getProductsBySellerAndStore`로 대체될 수 있습니다.
     *
     * @return 모든 상품 목록 (List<Products>)
     */
    @Override
    public List<Products> getProductList() {
        return productsMapper.getProductList();
    }

    /**
     * 고객용: 활성화되고 노출되는 상품 목록을 조회합니다.
     *
     * @return 활성화되고 노출되는 상품 목록 (List<Products>)
     */
    @Override
    public List<Products> getAllActiveProductsForCustomer() {
        return productsMapper.getAllActiveProductsForCustomer();
    }

    /**
     * 특정 상품의 상세 정보를 조회하고, 연관된 이미지 리스트를 함께 반환합니다.
     *
     * @param gdsNo 조회할 상품 번호
     * @return 상세 정보와 이미지 리스트가 담긴 Products 객체
     */
    @Override
    public Products getProductDetailWithImages(String gdsNo) {
        // ProductsMapper의 getProductDetailWithImages 메서드를 호출하여
        // 상품 상세 정보와 이미지 리스트를 함께 가져옵니다.
        return productsMapper.getProductDetailWithImages(gdsNo);
    }
}