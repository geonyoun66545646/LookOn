package ks55team02.seller.settlements.service;

import java.util.List;
import java.util.Map;

import ks55team02.seller.settlements.domain.SalesSttsDTO;
import ks55team02.seller.settlements.domain.SettlementSearchCriteria;

public interface SettlementService {
	
	 Map<String, Object> getMySettlementList(SettlementSearchCriteria criteria);
	 
	 String getStoreIdByUserNo(String userNo); // userNo를 받아서 storeId를 반환
	 
	 /**
     * 특정 상점의 판매 내역을 조회하는 비즈니스 로직
     * @param storeId 상점 ID
     * @return 판매 내역 DTO 목록
     */
    List<SalesSttsDTO> getSalesHistoryByStoreId(String storeId);
    
    /**
     * 모든 상점의 전체 판매 내역을 조회합니다.
     * @return 전체 판매 내역 DTO 목록
     */
    List<SalesSttsDTO> getAllSalesHistory();
}
