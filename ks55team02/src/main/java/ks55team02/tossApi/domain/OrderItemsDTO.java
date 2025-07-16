package ks55team02.tossApi.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderItemsDTO {
	
	private String ordrDtlArtclNo; // 주문 상세 항목 번호 (ordr_dtl_artcl_no)
    private String ordrNo;         // 주문 번호 (ordr_no)
    private String optNo;          // 주문한 상품 옵션번호 (opt_no)
    private String gdsNo;          // 원본 상품번호 (참조 용이성) (gds_no)
    private String storeId;        // 상점 ID (store_id)
    private int ordrQntty;         // 주문 수량 (ordr_qntty)
    private BigDecimal ordrTmUntprc; // 주문 시점의 단위 가격 (ordr_tm_untprc - DECIMAL(10,2)에 해당)
    private String ordrDtlArtclDcsnCd; // 주문 상세 항목 결정 (ordr_dtl_artcl_dcsn_cd)
	

}
