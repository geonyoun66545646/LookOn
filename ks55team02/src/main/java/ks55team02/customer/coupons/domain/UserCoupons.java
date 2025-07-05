package ks55team02.customer.coupons.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserCoupons {
    private String userCpnId;         // 사용자 쿠폰 ID (PK)
    private String userId;            // 사용자 아이디
    private String pblcnCpnId;        // 발행 쿠폰 ID (FK)
    private boolean useYn;            // 사용 여부
    private LocalDateTime cpnGiveDt;  // 쿠폰 지급 일시
    private LocalDateTime indivExpyDt; // 개별 만료 일시
    private LocalDateTime useDt;      // 사용 일시
    private LocalDateTime cpnMdfcnDt;   // 쿠폰 수정 일시
    private String issuRsnPrcCn;      // 발급 사유/처리 내용
    
    // JOIN을 통해 Coupon 테이블의 정보도 함께 가져올 수 있습니다.
    private Coupons coupons; // 쿠폰 상세 정보를 담을 객체
}