package ks55team02.admin.adminpage.storeadmin.premiumadd.service;

import java.util.List;

import ks55team02.admin.adminpage.storeadmin.premiumadd.domain.PremiumAddDTO;

public interface PremiumAddService {
	
	/**
     * 새로운 구독 플랜을 등록합니다.
     * @param premiumAddDTO 등록할 구독 플랜 정보
     * @return 등록 성공 여부 (true: 성공, false: 실패)
     */
    boolean registerSubscriptionPlan(PremiumAddDTO premiumAddDTO);

    /**
     * 모든 구독 플랜을 조회합니다.
     * @return 구독 플랜 목록
     */
    List<PremiumAddDTO> getAllPremiumAdds(); // 메소드명 변경: getPremiumAddDTO -> getAllPremiumAdds

    /**
     * ID로 특정 구독 플랜을 조회합니다.
     * @param sbscrPlanId 구독 플랜 ID
     * @return 조회된 구독 플랜 정보 또는 null
     */
    PremiumAddDTO getSubscriptionPlanById(String sbscrPlanId);

    /**
     * 구독 플랜 정보를 업데이트합니다.
     * @param premiumAddDTO 업데이트할 구독 플랜 정보
     * @return 업데이트 성공 여부
     */
    boolean updateSubscriptionPlan(PremiumAddDTO premiumAddDTO);

    /**
     * ID로 구독 플랜을 삭제합니다.
     * @param sbscrPlanId 삭제할 구독 플랜 ID
     * @return 삭제 성공 여부
     */
    boolean deleteSubscriptionPlan(String sbscrPlanId);
    
}
