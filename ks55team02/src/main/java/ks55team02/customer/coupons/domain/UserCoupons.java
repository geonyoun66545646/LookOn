package ks55team02.customer.coupons.domain;

import java.time.LocalDateTime;
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
}