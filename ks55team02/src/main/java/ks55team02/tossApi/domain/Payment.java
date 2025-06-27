package ks55team02.tossApi.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Payment {
	
	private String stlmId;          	// 결제 ID (payments 테이블의 PK)
    private String ordrNo;          	// 주문 번호
    private String userNo;          	// 사용자 번호
    private String stlmMthdCd;      	// 결제 수단 코드
    private BigDecimal stlmAmt;     	// 결제 금액
    private String stlmSttsCd;     	 	// 결제 상태 코드
    private String pgDlngId;        	// PG 거래 ID
    private String pgCoInfo;        	// PG사 정보
    private LocalDateTime stlmCmptnDt; 	// 결제 완료 일시
    private LocalDateTime stlmDmndDt;  	// 결제 요청 일시
    
}
