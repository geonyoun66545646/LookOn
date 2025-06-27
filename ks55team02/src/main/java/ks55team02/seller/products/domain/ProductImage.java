package ks55team02.seller.products.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductImage {
    private String imgNo; // 이미지 번호 (PK)
    private String gdsNo; // 상품 번호 (FK)
    private String creatrNo; // 생성자 번호
    private String mdfrNo; // 수정자 번호
    private String imgFilePathNm; // 이미지 파일 경로 및 이름
    private String videoUrl; // 비디오 URL (DB 스키마에 있음)
    private int imgIndctSn; // 이미지 표시 순서
    
    private LocalDateTime regDt; // 등록일시
    private LocalDateTime mdfcnDt; // 수정일시
    private LocalDateTime inactvtnDt; // 비활성화일시
    
    // private boolean rprsImgYn; // 📌 이 필드를 제거하거나 주석 처리합니다.
    
    // ✅ 새로 추가될 img_type 필드: ProductImageType 타입으로 변경
    private ProductImageType imgType;
    
    private boolean actvtnYn; // 활성화 여부
}