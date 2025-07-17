package ks55team02.admin.adminpage.useradmin.coupons.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data; 

@Data 
public class AdminCoupons {

    private String pblcnCpnId;
    private String userNo;
    private String cpnNm;
    private BigDecimal dscntVl;
    private BigDecimal minOrdrAmt;
    private BigDecimal maxDscntAmt;
    private LocalDateTime vldBgngDt;
    private LocalDateTime vldEndDt;
    private int totUseLmtNmtm;
    private int userPerUseLmtNmtm;
    private boolean actvtnYn;
    private LocalDateTime crtDt;
    private LocalDateTime mdfcnDt;
    private String reissueCycle;
    private String issueConditionType;
}