package ks55team02.customer.coupons.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Coupons {
    private String pblcnCpnId;
    private String userNo;
    private String cpnNm;
    private double dscntVl;
    private double minOrdrAmt;
    private double maxDscntAmt;
    private LocalDateTime vldBgngDt;
    private LocalDateTime vldEndDt;
    private int totUseLmtNmtm;
    private int userPerUseLmtNmtm;
    private boolean actvtnYn;
    private LocalDateTime crtDt;
    private LocalDateTime mdfcnDt;
    
    private boolean owned;

    
    private String issueConditionType; // issue_condition_type 컬럼과 연결될 필드
    private String reissueCycle;       // reissue_cycle 컬럼과 연결될 필드
    
    private boolean isIssuable;      // 프론트엔드에 전달할 쿠폰 발급 가능 여부
    private String statusMessage;    // 프론트엔드에 전달할 상태 메시지 (예: "금월 발급 완료")
    
}