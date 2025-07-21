package ks55team02.seller.premiumcheckout.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.seller.premiumcheckout.domain.SellerPremiumDTO;
import ks55team02.seller.premiumcheckout.domain.SellerPremiumPaymentDTO;

@Mapper
public interface SellerPremiumMapper {
	
	/**
     * 총 구독 플랜 개수를 조회합니다.
     * SearchCriteria에 포함된 검색 조건(searchKey, searchValue, startDate, endDate)을 활용합니다.
     * @param searchCriteria 검색 및 필터링 조건
     * @return 총 구독 플랜 개수
     */
    int getSubscriptionPlanCount(SearchCriteria searchCriteria);

    /**
     * 페이징 및 검색/정렬 조건에 맞는 구독 플랜 목록을 조회합니다.
     * SearchCriteria의 offset, pageSize, sortOrder, sortKey 등을 활용합니다.
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

    
    // 파라미터를 DTO 객체 하나로 받도록 변경합니다.
    void insertPremiumOrder(SellerPremiumPaymentDTO paymentOrder);

    // 결제 성공 시 DB 업데이트를 위한 메서드 (기존과 동일해도 무방)
    void updatePaymentSuccess(@Param("orderId") String orderId,
                              @Param("paymentKey") String paymentKey,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);
    
    // 주문 ID로 플랜 명칭을 조회합니다.
    String findPlanNameByOrderId(@Param("orderId") String orderId);
    
    //  planId로 구독 플랜의 상세 정보를 조회하는 메서드
    SellerPremiumDTO findPlanById(String planId);

    //  orderId로 결제 정보를 조회하는 메서드 (planId를 알아내기 위함)
    SellerPremiumPaymentDTO findPaymentById(String orderId);
    
    // 주문 ID로 결제 정보와 플랜 명칭을 함께 조회하는 메서드
    SellerPremiumPaymentDTO findPaymentWithPlanNameByOrderId(@Param("orderId") String orderId);
    
    /**
     * ✅ [이 메서드를 추가하세요]
     * 서비스 계층에서 수량에 따른 총 구독 기간을 계산할 때,
     * 이 플랜의 '기본 단위 기간'(예: 30일, 1년)이 며칠인지 DB에 물어보기 위한 기능입니다.
     * 
     * @param planId 기간을 조회할 플랜의 ID
     * @return 해당 플랜의 기간 값 (예: 30)
     */
    int findPlanTermById(@Param("planId") String planId);
}

