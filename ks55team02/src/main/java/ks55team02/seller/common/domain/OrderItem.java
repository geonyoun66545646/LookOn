package ks55team02.seller.common.domain;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Getter, Setter, EqualsAndHashCode, ToString 자동 생성
@NoArgsConstructor // 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 자동 생성
public class OrderItem {

    private String 				ordrDtlArtclNo; // 주문 상세 항목 번호
    private String 				ordrNo; // 주문 번호
    private String 				optNo; // 주문한 상품 옵션번호
    private String 				gdsNo; // 원본 상품번호 (참조 용이성)
    private String 				storeId; // 상점 ID
    private Integer 			ordrQntty; // 주문 수량
    private BigDecimal 			ordrTmUntprc; // 주문 시점의 단위 가격
    private String 				ordrDtlArtclDcsnCd; // 주문 상세 항목 결정
}