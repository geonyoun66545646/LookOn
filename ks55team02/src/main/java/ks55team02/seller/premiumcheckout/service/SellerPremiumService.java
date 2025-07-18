package ks55team02.seller.premiumcheckout.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.seller.premiumcheckout.domain.SellerPremiumDTO;
import ks55team02.seller.premiumcheckout.domain.SellerPremiumPaymentDTO;


@Service
public interface SellerPremiumService {
	
	/**
     * 총 구독 플랜 개수를 조회합니다.
     * @param searchCriteria 검색 조건
     * @return 총 구독 플랜 개수
     */
    int getSubscriptionPlanCount(SearchCriteria searchCriteria);

    /**
     * 페이징 및 검색/정렬 조건에 맞는 구독 플랜 목록을 조회합니다.
     * @param searchCriteria 페이징 및 정렬 조건
     * @return 구독 플랜 목록
     */
    List<SellerPremiumDTO> getSubscriptionPlanList(SearchCriteria searchCriteria);

    /**
     * 특정 구독 플랜 ID로 상세 정보를 조회합니다.
     * @param sbscrPlanId 조회할 구독 플랜 ID
     * @return SubscriptionPlan 객체 또는 null
     */
    SellerPremiumDTO getSubscriptionPlanById(String sbscrPlanId);
    
    // 주문 생성을 위한 서비스 메서드
    Map<String, Object> createPremiumOrder(Map<String, Object> payload, String userNo, String userName);

    //  결제 성공 처리를 위한 서비스 메서드    
    SellerPremiumPaymentDTO processPaymentSuccess(String orderId, String paymentKey, BigDecimal amount, String userNo);
}
