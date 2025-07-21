package ks55team02.orderproduct.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import ks55team02.tossapi.domain.PayOrderItemsDTO;
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
    private String emlAddr;
    private String gdsNm;
    
    /*tossapi의 orderItems 객체를 리스트로 가져와서 사용*/
    private List<PayOrderItemsDTO> payOrderItemsDTO;
}
