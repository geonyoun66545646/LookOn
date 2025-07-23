package ks55team02.seller.products.domain;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ProductRegistrationRequest {
 // 기본 정보
 private String productName; // gds_nm
 private String productCategory1; // ctgry_no (1차 카테고리)
 private String productCategory2; // ctgry_no (2차 카테고리)
 private String productDescription; // gds_expln
 private String gdsNo;
 
 // 미디어 업로드
 private List<MultipartFile> thumbnailImage; // 상품 썸네일 (1장)
 private List<MultipartFile> mainImage;      // 대표 이미지 (최소 1장, 최대 15장)
 private List<MultipartFile> detailImage;    // 상세 페이지 이미지 (최소 1장, 최대 20장)
 private String videoUrl; // 동영상 URL
 //⭐ 추가: 수정 시 삭제할 이미지의 ID 목록
 private List<String> deletedImageIds;
 // ⭐ 추가: 수정 시 유지할 기존 이미지의 경로 목록
 private List<String> existingImagePaths;

 // 가격 및 재고
 private Long basePrice;       // bas_prc
 private Double discountRate;  // dscnt_rt
 private Long finalPrice;      // last_sel_prc (계산될 값)
 private Integer stockQuantity = 0; // sel_psblty_qntty (product_status) (실 재고 수량)
 private Integer minPurchase = 1;   // 최소 구매 수량 (DB에 직접 매핑되는 필드는 아닐 수 있으나, 일단 받음)
 private Integer maxPurchase;   // 최대 구매 수량 (DB에 직접 매핑되는 필드는 아닐 수 있으나, 일단 받음)

 // 옵션 설정
 private String optionProductName; // opt_nm (product_options)
 private String genderOption;      // opt_vl_nm (성별 옵션 값)
 private List<String> colorOptions;  // opt_vl_nm (색상 옵션 값 목록)
 private String sizeOptionType;    // 사이즈 옵션 유형 (예: size_apparel, size_shoes)
 private List<String> sizeOptions;   // opt_vl_nm (사이즈 옵션 값 목록)
 //추가: 실제 선택된 옵션을 저장할 필드
 private List<String> selectedColorOptions;
 private List<String> selectedSizeOptions;

//⭐⭐⭐⭐⭐ [2단계: 필드 추가] 시작 ⭐⭐⭐⭐⭐
 private List<String> imageOrderMain;
 private List<String> imageOrderDetail;
 // ⭐⭐⭐⭐⭐ [2단계: 필드 추가] 끝 ⭐⭐⭐⭐⭐
 
 // 옵션 조합별 재고 (이 부분은 Map<String, Integer> 등으로 복합적으로 받아야 할 수 있음)
 // 예: "빨강_M": 10, "파랑_L": 5
 // 여기서는 일단 단순화하여, 각 조합은 서비스 로직에서 생성한다고 가정합니다.

 // 기타 설정
 private String productTags; // 태그 / 키워드
 private String productStatus; // 상품 상태 (disabled=승인대기, selling 등)

 // 사용자의 고유 번호 (로그인된 사용자 정보에서 가져온다고 가정)
 // 이 필드들은 실제 서비스에서는 세션 또는 스프링 시큐리티를 통해 가져와야 합니다.
 private String storeId;   // ⭐⭐⭐ 이 부분을 String으로 변경합니다. ⭐⭐⭐
 private String selUserNo; // ⭐⭐⭐ 이 부분도 String으로 변경합니다. ⭐⭐⭐

 // 이외에 DB에 필요한 필드 중 DTO에 없는 것은 서비스/매퍼 단에서 처리 (예: reg_dt, actvtn_yn 등)
 
 // 또는 setter 메서드에서 null 체크
 public void setStockQuantity(Integer stockQuantity) {
     this.stockQuantity = (stockQuantity != null) ? stockQuantity : 0;
 }
 
 private List<ProductCombinationData> productOptionCombinations;

 @Data // 내부 클래스에도 @Data 어노테이션을 붙여 Getter/Setter 등을 자동 생성
 public static class ProductCombinationData {
     private String combNm; // 조합 이름 (예: "제품명 + 성별 + 색상 + 사이즈")
     private String optVlNo1; // 옵션 값 No.1 (성별 ID)
     private String optVlNo2; // 옵션 값 No.2 (색상 ID)
     private String optVlNo3; // 옵션 값 No.3 (사이즈 ID)
     private int quantity;   // 재고 수량
 }
 
 // 편의 메서드 (finalPrice 계산)
 public void calculateFinalPrice() {
     if (basePrice != null && discountRate != null) {
         this.finalPrice = (long) (basePrice * (1 - discountRate / 100.0));
     } else {
         this.finalPrice = this.basePrice;
     }
 }
}
