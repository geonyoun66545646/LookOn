package ks55team02.customer.reports.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Reports {

    // DB 컬럼명과 1:1 매칭
    private String dclrId;              // dclr_id
    private String dclrUserNo;          // dclr_user_no
    private String dclrRsnCd;           // dclr_rsn_cd
    private String dclrTrgtUserNo;      // dclr_trgt_user_no
    private String dclrTrgtContsId;     // dclr_trgt_conts_id
    private String prcsPicMngrNo;       // prcs_pic_mngr_no (처리 담당 관리자 번호)
    private String dtlDclrRsnCn;        // dtl_dclr_rsn_cn
    private String prcsSttsCd;          // prcs_stts_cd
    private LocalDateTime dclrRcptDt;   // dclr_rcpt_dt
    private LocalDateTime prcsCmptnDt;  // prcs_cmptn_dt (처리 완료 일시)
    private String dclrTrgtTypeCd;      // dclr_trgt_type_cd
    private LocalDateTime mdfcnDt;      // mdfcn_dt (수정 일시)
    private String dclrPrcsRsltCn;      // dclr_prcs_rslt_cn
}