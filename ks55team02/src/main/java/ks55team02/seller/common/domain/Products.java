package ks55team02.seller.common.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Getter, Setter, EqualsAndHashCode, ToString 자동 생성
@NoArgsConstructor // 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 자동 생성
public class Products {

    private String 					gdsNo; // 상품 번호
    private String 					storeId; // 상점 번호
    private String 					ctgryNo; // 카테고리 번호
    private String 					selUserNo; // 판매자
    private String 					mdfrNo; // 수정자
    private String 					delUserNo; // 삭제자
    private String 					gdsNm; // 상품명
    private String 					gdsExpln; // 상품 설명
    private BigDecimal				basPrc; // 기본 가격
    private BigDecimal 				dscntRt; // 할인율(%)
    private BigDecimal 				lastSelPrc; // 최종 판매가
    private LocalDateTime 			regDt; // 등록일시
    private LocalDateTime 			mdfcnDt; // 수정일시
    private LocalDateTime 			inactvtnDt; // 비활성 일시
    private Boolean 				expsrYn; // 노출 여부 (tinyint(1)은 boolean으로 매핑)
    private Boolean 				actvtnYn; // 활성화 여부 (tinyint(1)은 boolean으로 매핑)
    private Integer 				minPurchaseQty; // 최소 구매 수량
    private Integer	 				maxPurchaseQty; // 최대 구매 수량 (NULL 허용하므로 Integer)
}