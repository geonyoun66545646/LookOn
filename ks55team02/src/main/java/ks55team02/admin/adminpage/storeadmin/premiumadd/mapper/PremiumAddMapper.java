package ks55team02.admin.adminpage.storeadmin.premiumadd.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import ks55team02.admin.adminpage.storeadmin.premiumadd.domain.PremiumAddDTO;

@Mapper
public interface PremiumAddMapper {
	
	/**
     * 새로운 구독 플랜을 등록합니다.
     * @param subscriptionPlanDTO 등록할 구독 플랜 정보
     * @return 삽입된 행의 수
     */
    int insertSubscriptionPlan(PremiumAddDTO premiumAddDTO);

    /**
     * 모든 구독 플랜 목록을 조회합니다.
     * @return 구독 플랜 목록
     */
    List<PremiumAddDTO> findAllSubscriptionPlans();

    /**
     * ID로 특정 구독 플랜을 조회합니다.
     * @param sbscrPlanId 구독 플랜 ID
     * @return 조회된 구독 플랜 정보 또는 null
     */
    PremiumAddDTO findSubscriptionPlanById(@Param("sbscrPlanId") String sbscrPlanId);

    /**
     * 구독 플랜 정보를 업데이트합니다.
     * @param subscriptionPlanDTO 업데이트할 구독 플랜 정보
     * @return 업데이트된 행의 수
     */
    int updateSubscriptionPlan(PremiumAddDTO premiumAddDTO);

    /**
     * ID로 구독 플랜을 삭제합니다.
     * @param sbscrPlanId 삭제할 구독 플랜 ID
     * @return 삭제된 행의 수
     */
    int deleteSubscriptionPlan(@Param("sbscrPlanId") String sbscrPlanId);
    
    
}
