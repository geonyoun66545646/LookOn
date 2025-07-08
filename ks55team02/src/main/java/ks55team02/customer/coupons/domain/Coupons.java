package ks55team02.customer.coupons.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Coupons {
    private String pblcnCpnId;
    private String pblcnCpnCd;
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
    
    private String issueConditionType; // issue_condition_type 컬럼과 연결될 필드
    private String reissueCycle;       // reissue_cycle 컬럼과 연결될 필드
}