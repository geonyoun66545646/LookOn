package ks55team02.seller.products.service.impl;

import ks55team02.seller.products.domain.*; // ProductImageType도 포함되어야 합니다.
import ks55team02.seller.products.domain.ProductRegistrationRequest;
import ks55team02.seller.products.mapper.ProductsMapper;
import ks55team02.seller.products.service.ProductsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성 주입
public class ProductsServiceImpl implements ProductsService {
    private final ProductsMapper productsMapper;

    // application.properties 또는 application.yml에 정의된 업로드 경로 주입
    @Value("${product.upload.dir}")
    private String uploadDir;

    /**
     * 판매자 및 스토어 ID에 해당하는 상품 목록을 조회합니다.
     * @param selUserNo 판매자 번호
     * @param storeId 스토어 ID
     * @return 조건에 맞는 상품 목록
     */
    @Override
    public List<Products> getProductsBySellerAndStore(String selUserNo, String storeId) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("selUserNo", selUserNo);
        paramMap.put("storeId", storeId);
        return productsMapper.getProductsBySellerAndStore(paramMap);
    }

    /**
     * 새로운 상품을 등록하고 관련 이미지, 옵션, 재고 정보를 처리합니다.
     * 이 메서드 내에서 여러 Mapper 메서드를 호출하며 트랜잭션으로 관리됩니다.
     * @param request 상품 등록에 필요한 모든 정보를 담은 요청 객체
     */
    @Override
    @Transactional // 메서드 실행 중 예외 발생 시 롤백
    public void addProduct(ProductRegistrationRequest request) {
        String selUserNo = request.getSelUserNo();
        String storeId = request.getStoreId();

        // 1. 상품 번호 생성 (항상 최신 번호를 가져오도록)
        String gdsNo = productsMapper.getMaxProductCode(); // 또는 getNewGdsNo

        // 2. 기본 상품 정보 설정 및 저장
        Products product = new Products();
        product.setGdsNo(gdsNo);
        product.setStoreId(storeId);
        // 카테고리 설정: productCategory2가 있으면 사용, 없으면 productCategory1 사용
        product.setCtgryNo(Objects.requireNonNullElse(request.getProductCategory2(), request.getProductCategory1()));
        product.setSelUserNo(selUserNo);
        product.setMdfrNo(selUserNo); // 수정자도 등록자와 동일하게 설정
        product.setGdsNm(request.getProductName());
        product.setGdsExpln(request.getProductDescription());
        product.setBasPrc(request.getBasePrice() != null ? request.getBasePrice().intValue() : 0);
        product.setDscntRt(request.getDiscountRate());

        // 최종 판매 가격 계산 (ProductRegistrationRequest 내에서 처리)
        request.calculateFinalPrice();
        product.setLastSelPrc(request.getFinalPrice() != null ? request.getFinalPrice().intValue() : 0);

        product.setRegDt(LocalDateTime.now());
        product.setExpsrYn(true); // 기본적으로 노출
        product.setActvtnYn(true); // 기본적으로 활성화

        productsMapper.addProduct(product); // 상품 기본 정보 DB 삽입

        // 3. 이미지 업로드 및 DB 저장
        // 이미지 저장 순서 (img_indct_sn)를 위한 카운터
        int imgIndctSn = 1;

        // 썸네일 이미지 처리 (ProductImageType.THUMBNAIL)
        if (request.getThumbnailImage() != null) {
            for (MultipartFile file : request.getThumbnailImage()) {
                // saveAndInsertImage 메서드 호출 시 ProductImageType.THUMBNAIL 전달
                saveAndInsertImage(file, gdsNo, selUserNo, productsMapper.getMaxImageNo(), imgIndctSn++, ProductImageType.THUMBNAIL);
            }
        }
        // 메인 이미지 처리 (ProductImageType.MAIN)
        if (request.getMainImage() != null) {
            for (MultipartFile file : request.getMainImage()) {
                // saveAndInsertImage 메서드 호출 시 ProductImageType.MAIN 전달
                saveAndInsertImage(file, gdsNo, selUserNo, productsMapper.getMaxImageNo(), imgIndctSn++, ProductImageType.MAIN);
            }
        }
        // 상세 이미지 처리 (ProductImageType.DETAIL)
        if (request.getDetailImage() != null) {
            for (MultipartFile file : request.getDetailImage()) {
                // saveAndInsertImage 메서드 호출 시 ProductImageType.DETAIL 전달
                saveAndInsertImage(file, gdsNo, selUserNo, productsMapper.getMaxImageNo(), imgIndctSn++, ProductImageType.DETAIL);
            }
        }

        // 4. 옵션 정보 생성 및 저장
        int optOrder = 1;
        // 성별 옵션 처리
        if (request.getGenderOption() != null && !request.getGenderOption().isEmpty()) {
            String genderOptNo = productsMapper.getMaxOptionNo();
            // 단일 선택('S') 옵션
            insertOptionAndValue(gdsNo, selUserNo, "성별", "S", optOrder++, genderOptNo, List.of(request.getGenderOption()));
        }
        // 색상 옵션 처리
        if (request.getColorOptions() != null && !request.getColorOptions().isEmpty()) {
            String colorOptNo = productsMapper.getMaxOptionNo();
            // 단일 선택('S') 옵션 (또는 복수 선택 'M'으로 변경 가능)
            insertOptionAndValue(gdsNo, selUserNo, "색상", "S", optOrder++, colorOptNo, request.getColorOptions());
        }
        // 사이즈 옵션 처리
        if (request.getSizeOptions() != null && !request.getSizeOptions().isEmpty()) {
            String sizeOptNo = productsMapper.getMaxOptionNo();
            // 단일 선택('S') 옵션 (또는 복수 선택 'M'으로 변경 가능)
            insertOptionAndValue(gdsNo, selUserNo, "사이즈", "S", optOrder++, sizeOptNo, request.getSizeOptions());
        }

        // 5. 상품 재고 상태 저장
        String gdsSttsNo = productsMapper.getMaxStatusNo();
        if (gdsSttsNo == null || gdsSttsNo.isEmpty()) {
            throw new RuntimeException("상품 상태 번호(gdsSttsNo)를 생성할 수 없습니다. getMaxStatusNo() 쿼리를 확인하세요.");
        }

        ProductStatus productStatus = new ProductStatus();
        productStatus.setGdsSttsNo(gdsSttsNo);
        productStatus.setGdsNo(gdsNo);
        productStatus.setCreatrNo(selUserNo);
        productStatus.setMdfrNo(selUserNo);
        productStatus.setSelPsbltyQntty(request.getStockQuantity());
        productStatus.setRegDt(LocalDateTime.now());
        productStatus.setSldoutYn(request.getStockQuantity() != null && request.getStockQuantity() <= 0); // 재고 0이하 시 품절
        productStatus.setActvtnYn(true);

        productsMapper.insertProductStatus(productStatus);

        // 6. 옵션 조합 상태 매핑 (TODO: 상품 옵션 조합에 따른 재고 관리 로직을 여기에 구현)
        // 이 부분은 프론트엔드에서 어떤 방식으로 옵션 조합 및 재고를 받을지에 따라 달라지므로,
        // 현재는 구현을 생략하거나 Map을 사용하여 임시로 처리할 수 있습니다.
        // 예: productsMapper.insertStatusOptionMapping(Map<String, Object> paramMap);
    }

    /**
     * 이미지 파일을 저장하고 DB에 정보를 삽입하는 헬퍼 메서드
     * ✅ isRepresentative 대신 ProductImageType를 직접 받도록 변경
     * @param file 멀티파트 파일 객체
     * @param gdsNo 상품 번호
     * @param creatorNo 생성자(판매자) 번호
     * @param imgNo 이미지 번호 (DB에서 생성된 값)
     * @param imgIndctSn 이미지 순서 (img_indct_sn)
     * @param imageType 이미지 타입 (THUMBNAIL, MAIN, DETAIL 등)
     */
    private void saveAndInsertImage(MultipartFile file, String gdsNo, String creatorNo, String imgNo, int imgIndctSn, ProductImageType imageType) {
        if (file.isEmpty()) return; // 파일이 비어있으면 처리하지 않음

        Path filePath = null; // 파일 경로 변수 초기화

        try {
            // 업로드 디렉토리 생성 (없으면)
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

            // 저장될 파일 이름: 이미지 번호 + 확장자
            String savedFileName = imgNo + ext;
            filePath = uploadPath.resolve(savedFileName); // 최종 파일 경로 설정

            // 파일 복사 (기존 파일이 있으면 덮어씀)
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // DB에 이미지 정보 삽입
            ProductImage productImage = new ProductImage();
            productImage.setImgNo(imgNo);
            productImage.setGdsNo(gdsNo);
            productImage.setCreatrNo(creatorNo);
            productImage.setMdfrNo(creatorNo);
            // 웹 접근 경로 설정 (예: /images/product_upload/파일이름.확장자)
            productImage.setImgFilePathNm("/images/product_upload/" + savedFileName);
            productImage.setImgIndctSn(imgIndctSn); // 이미지 순서 설정
            productImage.setRegDt(LocalDateTime.now());
            // ✅ ProductImageType 설정
            productImage.setImgType(imageType);
            productImage.setActvtnYn(true);

            productsMapper.insertProductImage(productImage);

        } catch (IOException e) {
            // 이미지 처리 중 오류 발생 시 런타임 예외 발생
            String errorFilePath = (filePath != null) ? filePath.toString() : "알 수 없는 파일 경로";
            throw new RuntimeException("이미지 처리 중 오류 발생: " + errorFilePath, e);
        }
    }

    /**
     * 상품 옵션과 그 값들을 데이터베이스에 삽입하는 헬퍼 메서드
     * @param gdsNo 상품 번호
     * @param userNo 생성자(판매자) 번호
     * @param optionName 옵션 이름 (예: "색상", "사이즈")
     * @param choiceType 선택 유형 (예: "S": 단일 선택, "M": 복수 선택)
     * @param order 옵션 순서
     * @param optNo 생성된 옵션 번호
     * @param values 옵션 값 목록 (예: ["빨강", "파랑"], ["S", "M", "L"])
     */
    private void insertOptionAndValue(String gdsNo, String userNo, String optionName, String choiceType, int order, String optNo, List<String> values) {
        // 1. ProductOption 정보 설정 및 삽입
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

        productsMapper.insertProductOption(option); // 상품 옵션 DB 삽입

        // 2. ProductOptionValue 정보 설정 및 삽입
        for (String val : values) {
            ProductOptionValue value = new ProductOptionValue();
            value.setOptVlNo(productsMapper.getMaxOptionValueNo()); // 옵션 값 번호 생성
            value.setOptNo(optNo); // ✅ 이 부분, 생성된 옵션 번호(optNo)를 옵션 값에 연결
            value.setCreatrNo(userNo);
            value.setMdfrNo(userNo);
            value.setVlNm(val);
            value.setRegDt(LocalDateTime.now());
            value.setActvtnYn(true);

            productsMapper.insertProductOptionValue(value); // 상품 옵션 값 DB 삽입
        }
    }

    /**
     * 특정 상품 코드가 이미 존재하는지 확인합니다.
     * @param productCode 확인할 상품 코드
     * @return true면 중복, false면 중복 아님
     */
    @Override
    public boolean isProductCodeDuplicated(String productCode) {
        return productsMapper.countProductCode(productCode) > 0;
    }

    /**
     * 판매자/관리자용: 모든 상품 목록을 조회합니다. (필요시 사용)
     * @return 모든 상품 목록
     */
    @Override
    public List<Products> getProductList() {
        return productsMapper.getProductList();
    }

    /**
     * 고객용: 활성화되고 노출되는 상품 목록을 조회합니다.
     * @return 활성화되고 노출되는 상품 목록
     */
    @Override
    public List<Products> getAllActiveProductsForCustomer() {
        return productsMapper.getAllActiveProductsForCustomer();
    }

    // ---- 상품 상세 조회 기능 추가 ----

    /**
     * 상품 상세 정보와 이미지 리스트를 함께 조회합니다.
     * @param gdsNo 상품 번호
     * @return 상품 상세 정보와 이미지 리스트
     */
    @Override
    public Products getProductDetailWithImages(String gdsNo) {
        // ProductsMapper의 getProductDetailWithImages 메서드를 호출하여
        // 상품 상세 정보와 이미지 리스트를 함께 가져옵니다.
        return productsMapper.getProductDetailWithImages(gdsNo);
    }
}