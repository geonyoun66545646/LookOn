package ks55team02.seller.common.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Goods {
    private String gdsNo;
    private String storeId;
    private String ctgryNo;
    private String selUserNo;
    private String mdfrNo;
    private String delUserNo;
    private String gdsNm;
    private String gdsExpln;
    private BigDecimal basPrc;
    private BigDecimal dscntRt;
    private BigDecimal lastSelPrc;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
    private LocalDateTime inactvtnDt;
    private Boolean expsrYn;
    private Boolean actvtnYn;
    private Integer minPurchaseQty;
    private Integer maxPurchaseQty;

}