// ProductApprovalHistory.java

package ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductApprovalHistory {
    private String aprvRjctHstryCd;
    private String gdsNo;
    private String prcsMngrId;
    private String aprvSttsCd;
    private LocalDateTime prcsDt;
    private String rjctRsn;
    private Integer aprvRjctCycl;
    private String mngrCmntCn;

    // [추가] 화면에 상품명을 표시하기 위한 필드
    private String gdsNm;
}