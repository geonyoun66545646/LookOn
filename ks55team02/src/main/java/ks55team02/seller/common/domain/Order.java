package ks55team02.seller.common.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Getter, Setter, EqualsAndHashCode, ToString 자동 생성
@NoArgsConstructor // 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 자동 생성
public class Order {

    private String 				ordrNo; // 주문 번호
    private String 				userNo; // 주문 사용자 번호
    private LocalDateTime 		ordrDt; // 주문 일시
    private BigDecimal 			gdsTotAmt; // 총 상품 금액
    private BigDecimal 			dlvyFeeAmt; // 배송비
    private String 				pblcnCpnId; // 발행 쿠폰 ID
    private String 				userCpnId; // 적용된 쿠폰
    private BigDecimal 			apldCpnDscntAmt; // 적용된 쿠폰 할인 금액
    private BigDecimal 			lastStlmAmt; // 최종 결제 금액
    private String 				ordrSttsCd; // 주문 상태
    private String 				rcvrNm; // 수령인 이름
    private String 				rcvrTelno; // 수령인 연락처
    private String 				dlvyAddr; // 배송 주소
    private String 				dlvyDaddr; // 배송 상세 주소
    private String 				zip; // 우편번호
    private String 				dlvyMemoCn; // 배송 메모
    private String 				userName; // 사용자 이름
}