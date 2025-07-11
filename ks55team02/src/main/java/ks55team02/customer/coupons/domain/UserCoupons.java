package ks55team02.customer.coupons.domain;

import java.time.LocalDateTime;

import org.springframework.data.relational.core.mapping.Column;

import lombok.Data;

@Data
public class UserCoupons {
    private String userCpnId;
    private String userNo; // userId -> userNo
    private String pblcnCpnId;
    private boolean useYn;
    private LocalDateTime cpnGiveDt;
    private LocalDateTime indivExpryDt; // indivExpyDt -> indivExpryDt
    private LocalDateTime useDt;
    private LocalDateTime cpnMdfcnDt;
    private String issuRsnSrcCn; // issuRsnPrcCn -> issuRsnSrcCn

    // JOIN을 통해 Coupon 테이블의 정보도 함께 가져올 수 있습니다.
    private Coupons coupon;
    
    // - 2025.07.11 gy -
    @Column(value = "use_yn")
    private boolean used;  // true=1 (사용완료), false=0 (사용가능) → 필드명 변경으로 혼동 방지
    
}