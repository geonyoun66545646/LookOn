// AdminPageProductadminAdminproductsmanagementDomainApprovalCriteria.java (이름이 너무 길 경우 적절히 줄여서 사용)
// 혹은 admin.adminpage.productadmin.domain 패키지를 만들어서 해당 도메인만 관리
package ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApprovalCriteria {
    private String aprvRjctRsnCd; // aprv_rjct_rsn_cd
    private String crtMngrId;    // crt_mngr_id
    private String mdfcnMngrId;  // mdfcn_mngr_id
    private String delMngrId;    // del_mngr_id
    private String crtrTypeCd;   // crtr_type_cd
    private String aplcnTrgtId;  // aplcn_trgt_id (PRODUCT, SELLER 등)
    private String aprvRjctDtlCn; // aprv_rjct_dtl_cn (상세 설명)
    private String aprvRjctRsn;  // aprv_rjct_rsn (승인/반려 여부 텍스트 - "반려")
    private LocalDateTime crtYmd; // crt_ymd
    private LocalDateTime inactvtnDt; // inactvtn_dt
    private LocalDateTime mdfcnDt;    // mdfcn_dt
    private Boolean aplcnYn;     // aplcn_yn (활성화 여부)
}