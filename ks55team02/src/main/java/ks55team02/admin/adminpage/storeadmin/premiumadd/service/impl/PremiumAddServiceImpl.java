package ks55team02.admin.adminpage.storeadmin.premiumadd.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.storeadmin.premiumadd.domain.PremiumAddDTO;
import ks55team02.admin.adminpage.storeadmin.premiumadd.mapper.PremiumAddMapper;
import ks55team02.admin.adminpage.storeadmin.premiumadd.service.PremiumAddService;

@Service
public class PremiumAddServiceImpl implements PremiumAddService {
	 
	private final PremiumAddMapper premiumAddMapper;

    @Autowired
    public PremiumAddServiceImpl(PremiumAddMapper premiumAddMapper) {
        this.premiumAddMapper = premiumAddMapper;
    }

    @Override // 인터페이스 메소드 오버라이드
    @Transactional
    public boolean registerSubscriptionPlan(PremiumAddDTO premiumAddDTO) {
        // ID가 없으면 새로 생성 (UUID)
    	premiumAddDTO.generateId();

        // 유효성 검사
        if (premiumAddDTO.getSbscrPlanId() == null || premiumAddDTO.getSbscrPlanId().isEmpty() ||
        	premiumAddDTO.getSbscrPlanNm() == null || premiumAddDTO.getSbscrPlanNm().isEmpty() ||
        	premiumAddDTO.getSbscrPlanTermVal() == null || premiumAddDTO.getSbscrPlanTermVal() <= 0 ||
        	premiumAddDTO.getSbscrPlanPrc() == null || premiumAddDTO.getSbscrPlanPrc().compareTo(BigDecimal.ZERO) < 0) {
            // 필수 필드 누락 또는 유효하지 않은 값
            return false;
        }
        return premiumAddMapper.insertSubscriptionPlan(premiumAddDTO) == 1;
    }

    @Override // 인터페이스 메소드 오버라이드
    public List<PremiumAddDTO> getAllPremiumAdds() { // 메소드명 변경 및 @Override 추가
        return premiumAddMapper.findAllSubscriptionPlans();
    }

    @Override // 인터페이스 메소드 오버라이드
    public PremiumAddDTO getSubscriptionPlanById(String sbscrPlanId) {
        return premiumAddMapper.findSubscriptionPlanById(sbscrPlanId); // premiumAddMapper로 변경
    }

    @Override // 인터페이스 메소드 오버라이드
    @Transactional
    public boolean updateSubscriptionPlan(PremiumAddDTO premiumAddDTO) { // 파라미터 타입 PremiumAddDTO로 변경
        if (premiumAddDTO.getSbscrPlanId() == null || premiumAddDTO.getSbscrPlanId().isEmpty()) {
            return false; // ID가 없으면 업데이트 불가
        }
        return premiumAddMapper.updateSubscriptionPlan(premiumAddDTO) == 1; // premiumAddMapper로 변경
    }

    @Override // 인터페이스 메소드 오버라이드
    @Transactional
    public boolean deleteSubscriptionPlan(String sbscrPlanId) {
        return premiumAddMapper.deleteSubscriptionPlan(sbscrPlanId) == 1; // premiumAddMapper로 변경
    }
    
    @Override
    public PremiumAddDTO getPlanById(String planId) {
        return premiumAddMapper.getPlanById(planId);
    }

    @Override
    public boolean modifySubscriptionPlan(String planId, PremiumAddDTO modifiedPlan) {
        modifiedPlan.setSbscrPlanId(planId); // ID 설정
        int result = premiumAddMapper.modifySubscriptionPlan(modifiedPlan);
        return result > 0;
    }
}