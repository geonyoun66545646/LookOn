package ks55team02.orderproduct.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderItemDTO {
	
	private String orderItemId;             // 주문 상세 항목 번호 (ordr_dtl_artcl_no)
    private String orderId;                 // 주문 번호 (ordr_no)
    private String optionNo;                // 주문한 상품 옵션번호 (opt_no)
    private String goodsNo;                 // 원본 상품번호 (gds_no)
    private String storeId;                 // 상점 ID (store_id)
    private int orderQuantity;              // 주문 수량 (ordr_qntty)
    private BigDecimal orderTimeUnitPrice;  // 주문 시점의 단위 가격 (ordr_tm_untprc)
    private String orderItemDecisionCode;   // 주문 상세 항목 결정 (ordr_dtl_artcl_dcsn_cd)
    
}
