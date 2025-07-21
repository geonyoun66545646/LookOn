package ks55team02.seller.premiumcheckout.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SellerPremiumDTO {
	
	private String sbscrPlanId;           // 구독 플랜 ID
    private String sbscrPlanNm;           // 구독 플랜 명칭
    private BigDecimal sbscrPlanPrc;      // 구독권(플랜) 가격
    private Integer sbscrPlanTermVal;     // 구독권(플랜) 기간
    private String sbscrPlanExpln;        // 구독권(플랜) 설명
    private LocalDateTime sbscrPlanCrtDt; // 구독권(플랜) 생성 일시
    private LocalDateTime sbscrPlanMdfcnDt; // 구독권(플랜) 수정 일시
    
}
