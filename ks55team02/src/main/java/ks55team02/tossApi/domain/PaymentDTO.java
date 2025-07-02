package ks55team02.tossApi.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PaymentDTO {
	
	private String stlmId; // 결제 ID (우리가 생성 또는 토스로부터 받음)
    private String ordrNo; // 주문 번호
    private String userNo; // 사용자 번호 (임시로 하드코딩)
    private String stlmMthdCd; // 결제 수단 (CARD, TRANSFER 등)
    private BigDecimal stlmAmt; // 결제 금액
    private String stlmSttsCd; // 결제 상태 (DONE, CANCELED 등)
    private String pgDlngId; // PG 거래 ID (토스 paymentKey)
    private String pgCoInfo; // PG사 정보 (Toss Payments)
    private LocalDateTime stlmCmptnDt; // 결제 완료 일시
    private LocalDateTime stlmDmndDt; // 결제 요청 일시
    
}
