package ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductApprovalHistory {
    private String aprvRjctHstryCd; // aprv_rjct_hstry_cd (PK)
    private String gdsNo;           // gds_no (상품 번호)
    private String prcsMngrId;      // prcs_mngr_id (처리 관리자 ID)
    private String aprvSttsCd;      // aprv_stts_cd (승인 상태 코드: "대기", "승인", "반려")
    private LocalDateTime prcsDt;   // prcs_dt (처리 일시)
    private String rjctRsn;         // rjct_rsn (반려 요약 사유, 예: "규격 미달")
    private Integer aprvRjctCycl;   // aprv_rjct_cycl (승인/반려 차수)
    private String mngrCmntCn;      // mngr_cmnt_cn (관리자 코멘트/상세 내용)
}