package ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain;

import lombok.Data;
import java.util.List;

@Data
public class ProductRejectRequest {
    private String gdsNo;             // 반려할 상품 번호
    private List<String> rejectReasonCodes; // 선택된 반려 사유 코드 목록 (RJCT_CD_xxx)
    private String managerComment;    // 관리자가 직접 입력하는 상세 반려 내용 (mngr_cmnt_cn)
    //private String managerId;        // 컨트롤러에서 세션 등을 통해 주입받을 예정
}