package ks55team02.admin.adminpage.storeadmin.premiumadd.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class PremiumAddDTO {
	
	private String sbscrPlanId;         // sbscr_plan_id (VARCHAR(50) PRIMARY KEY)
    private String sbscrPlanNm;         // sbscr_plan_nm (VARCHAR(100))
    private BigDecimal sbscrPlanPrc;    // sbscr_plan_prc (DECIMAL(10,2))
    private Integer sbscrPlanTermVal;   // sbscr_plan_term_val (INT)
    private String sbscrPlanExpln;      // sbscr_plan_expln (VARCHAR(255))
    private LocalDateTime sbscrPlanCrtDt; // sbscr_plan_crt_dt (DATETIME)
    private LocalDateTime sbscrPlanMdfcnDt; // sbscr_plan_mdfcn_dt (DATETIME)
    
    // 새로운 플랜 생성 시 ID 자동 생성을 위한 편의 메서드
    public void generateId() {
        if (this.sbscrPlanId == null || this.sbscrPlanId.isEmpty()) {
            this.sbscrPlanId = UUID.randomUUID().toString();
        }
    }
    
}
