package ks55team02.seller.settlements.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SalesSttsDTO {
	
	private String orderItemId;   // order_items.ordr_dtl_artcl_no
    private String orderId;       // orders.ordr_no
    private LocalDateTime orderDate;    // orders.ordr_dt
    private String productId;     // products.gds_no
    private String productName;   // products.gds_nm (JOIN을 통해 가져옴)
    private int quantity;         // order_items.ordr_qntty
    private BigDecimal unitPrice;     // order_items.ordr_tm_untprc
    private BigDecimal totalAmount;   // (수량 * 단가) - 쿼리에서 계산된 값
    
    private String storeName; // stores.store_conm
}
