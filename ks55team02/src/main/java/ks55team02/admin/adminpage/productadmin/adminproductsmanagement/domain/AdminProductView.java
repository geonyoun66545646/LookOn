package ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminProductView {
    private String gdsNo;
    private String gdsNm;
    private LocalDateTime regDt;
    private String storeConm;
    private String selUserNo;
    private String aprvSttsCd;
    private Integer aprvRjctCycl;
    
    // [추가] '전체 상품 관리' 페이지에서 사용할 필드
    private Long finalPrice;      // 최종 판매가 (last_sel_prc)
    private Integer stockQuantity; // 총 재고 (sel_psblty_qntty)
    private String statusCd;      // 계산된 최종 상태 (예: '판매중', '판매중지')
}