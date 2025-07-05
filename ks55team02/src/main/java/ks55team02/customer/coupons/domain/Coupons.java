package ks55team02.customer.coupons.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Coupons {
    private String pblcnCpnCd;        // 발행 쿠폰 코드 (PK)
    private String pblcnCpnNm;        // 발행 쿠폰명
    private String userId;            // 발행 관리자 아이디
    private String cpnNm;             // 쿠폰명
    private String dscntTypeCd;       // 할인 유형 (AMOUNT: 금액, RATE: 비율)
    private double dscntVl;           // 할인 값 (금액 또는 비율)
    private double minOrdAmnt;        // 최소 주문 금액
    private double maxDscntAmnt;      // 최대 할인 금액
    private LocalDateTime vldBgngDt;  // 유효 시작일
    private LocalDateTime vldEndDt;   // 유효 종료일
    private int usePsblImtNmtm;     // 사용 가능 제한 횟수
    private int userPerUseLmtNmtm;    // 사용자당 사용 제한 횟수
    private boolean actvtnYn;         // 활성화 여부
    private LocalDateTime crtDt;      // 생성일
    private LocalDateTime mdfcnDt;    // 수정일
}