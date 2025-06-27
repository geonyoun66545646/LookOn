package ks55team02.seller.products.domain;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List; // List를 사용하기 위해 임포트가 필요합니다.

@Data
public class Products {
    private String gdsNo;
    private String storeId;
    private String ctgryNo;
    private String selUserNo;
    private String creatrNo;
    private String mdfrNo;
    private String delUserNo;
    private String gdsNm;
    private String gdsExpln;
    private Integer basPrc;
    private Double dscntRt;
    private Integer lastSelPrc;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
    private LocalDateTime inactvtnDt;
    private Boolean expsrYn; // Boolean 타입 (래퍼 클래스)
    private Boolean actvtnYn; // Boolean 타입 (래퍼 클래스)

    // ⭐⭐⭐ 이 부분을 추가해야 합니다 ⭐⭐⭐
    private String thumbnailImagePath; // 고객용 상품 목록에서 썸네일 경로를 직접 사용하기 위함

    private ProductCategory productCategory; // 카테고리 정보 객체
    private List<ProductImage> productImages; // 상품 이미지 리스트

    // ⭐⭐⭐ 새로 추가할 ProductOption 리스트 필드 ⭐⭐⭐
    private List<ProductOption> productOptions; // 이 필드가 있어야 옵션 정보를 가져올 수 있습니다.

    // ⭐⭐ 수동으로 추가하는 getter 메서드 (문제 해결용) ⭐⭐
    public Boolean getExpsrYn() {
        return this.expsrYn;
    }
    
    public Boolean getActvtnYn() {
        return this.actvtnYn;
    }
}