package ks55team02.seller.common.service.impl;

import java.util.Map;

import ks55team02.seller.common.domain.Store;

public interface StoreMngService {

	// List<Store> getSotreList();
	Store getStoreInfoById(String storeId);
	
	// 특정 상점 ID에 대한 구독 기간 통합 정보를 조회합니다.
	Map<String, Object> getStoreSubscriptionByStoreId(String storeId);
}
