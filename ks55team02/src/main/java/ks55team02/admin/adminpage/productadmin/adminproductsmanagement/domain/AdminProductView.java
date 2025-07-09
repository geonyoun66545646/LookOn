package ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain;


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminProductView {
    private String gdsNo;
    private String gdsNm;
    private LocalDateTime regDt;
    
    // ⭐ 수정: storeName -> storeConm 으로 필드명 변경
    private String storeConm;   // 상점명(브랜드명)
    
    private String selUserNo;
    private String aprvSttsCd;
    
    private Integer aprvRjctCycl;
}