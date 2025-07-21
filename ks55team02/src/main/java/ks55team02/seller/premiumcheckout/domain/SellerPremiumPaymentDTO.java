package ks55team02.seller.premiumcheckout.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SellerPremiumPaymentDTO {
	private String sbscrStlmId; // 구독권 결제 ID
    private String userNo; // 사용자 번호
    private String sbscrPlanId; // 구독 플랜 ID
    private Integer sbscrPrchsNocs; // 구독 구매 개수
    private LocalDate sbscrBgngDt; // 구독 시작 일시
    private LocalDate sbscrEndDt; // 구독 종료 일시
    private BigDecimal sbscrTotStlmAmt; // 총 결제 금액
    private String sbscrStlmMthdCd; // 결제 수단
    private String sbscrStlmSttsCd; // 결제 상태
    private String pgDlngId; // PG 거래 ID
    private String pgCoInfo; // PG사 정보
    private LocalDateTime stlmCmptnDt; // 결제 완료 일시
    private LocalDateTime stlmDmndDt; // 결제 요청 일시
    private String sbscrPlanNm; // 구독 플랜 명칭
}
