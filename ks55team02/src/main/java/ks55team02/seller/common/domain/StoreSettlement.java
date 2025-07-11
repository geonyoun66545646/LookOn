package ks55team02.seller.common.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class StoreSettlement {

	private String 			storeClclnId; // 스토어 정산 아이디 (store_clcln_id)
    private String 			storeId; // 상점 ID (store_id)
    private String 			plcyId; // 정책 ID (plcy_id)
    private BigDecimal 		totSelAmt; // 총 판매 금액 테이블 (tot_sel_amt)
    private BigDecimal 		selFeeRt; // 판매 수수료율(%) (sel_fee_rt)
    private BigDecimal 		clclnAmt; // 정산 금액 (clcln_amt)
    private String 			actnoId; // 계좌 ID (actno_id)
    private LocalDate 		clclnPrcsYmd; // 정산 처리일 (clcln_prcs_ymd)
    private String 			clclnSttsCd; // 정산 상태 (clcln_stts_cd)
}
