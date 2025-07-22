package ks55team02.seller.settlements.service;

import java.util.Map;

import ks55team02.seller.settlements.domain.SettlementSearchCriteria;

public interface SettlementService {
	
	 Map<String, Object> getMySettlementList(SettlementSearchCriteria criteria);
	 
	 String getStoreIdByUserNo(String userNo); // userNo를 받아서 storeId를 반환
}
