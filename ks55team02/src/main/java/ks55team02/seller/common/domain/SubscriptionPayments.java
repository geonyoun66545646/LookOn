package ks55team02.seller.common.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SubscriptionPayments {

	private String 			sbscrStlmId; // 구독권 결제 ID (sbscr_stlm_id)
    private String 			userNo; // 사용자 번호 (user_no)
    private String 			sbscrPlanId; // 구독 플랜 ID (sbscr_plan_id)
    private Integer 		sbscrPrchsNocs; // 구독 구매 개수 (sbscr_prchs_nocs)
    private LocalDate 		sbscrBgngDt; // 구독 시작 일시 (sbscr_bgng_dt)
    private LocalDate 		sbscrEndDt; // 구독 종료 일시 (sbscr_end_dt)
    private BigDecimal 		sbscrTotStlmAmt; // 총 결제 금액 (sbscr_tot_stlm_amt)
    private String 			sbscrStlmMthdCd; // 결제 수단 코드 (sbscr_stlm_mthd_cd)
    private String 			sbscrStlmSttsCd; // 결제 상태 코드 (sbscr_stlm_stts_cd)
    private String 			pgDlngId; // PG 거래 ID (pg_dlng_id)
    private String 			pgCoInfo; // PG사 정보 (pg_co_info)
    private LocalDateTime 	stlmCmptnDt; // 결제 완료 일시 (stlm_cmptn_dt)
    private LocalDateTime 	stlmDmndDt; // 결제 요청 일시 (stlm_dmnd_dt)
}
