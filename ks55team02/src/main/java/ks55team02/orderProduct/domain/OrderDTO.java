package ks55team02.orderProduct.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OrderDTO {
	private String ordrNo;
    private String userNo;
    private LocalDateTime ordrDt;
    private BigDecimal gdsTotAmt;
    private BigDecimal dlvyFeeAmt;
    private String pblcnCpnId;
    private String userCpnId;
    private BigDecimal apldCpnDscntAmt;
    private BigDecimal lastStlmAmt;
    private String ordrSttsCd;
    private String rcvrNm;
    private String rcvrTelno;
    private String dlvyAddr;
    private String dlvyDaddr;
    private String zip;
    private String dlvyMemoCn;
    private String userName;
}
