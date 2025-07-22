package ks55team02.seller.settlements.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class SettlementDTO {
	
	private String storeClclnId; // store_clcln_id
    private String storeId;      // store_id
    private String storeConm;      // 상점명 (조인해서 가져와야 할 수도 있습니다)
    private String plcyId;			// 수수료
    private BigDecimal totSelAmt; // tot_sel_amt
    private BigDecimal selFeeRt;  // sel_fee_rt
    private BigDecimal clclnAmt;  // clcln_amt
    private LocalDate clclnPrcsYmd; // clcln_prcs_ymd
    private String clclnSttsCd;  // clcln_stts_cd

}
