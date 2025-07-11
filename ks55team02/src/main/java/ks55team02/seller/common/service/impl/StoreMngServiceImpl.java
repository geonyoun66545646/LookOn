package ks55team02.seller.common.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import ks55team02.seller.common.domain.Store;
import ks55team02.seller.common.mapper.StoreMngMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class StoreMngServiceImpl implements StoreMngService{
	
	private final StoreMngMapper storeMngMapper;
	
	/*
	 * 
	 * @Override public List<Store> getSotreList() { // TODO Auto-generated method
	 * stub return null; }
	 */
	
	@Override
	public Store getStoreInfoById(String storeId) {
		Store store = storeMngMapper.getStoreInfoById(storeId);
		
		return store;
	}
	
	@Override
	public Map<String, Object> getStoreSubscriptionByStoreId(String storeId) {
        log.info("getStoreSubscriptionByStoreId: {}", storeId);
        return storeMngMapper.getStoreSubscriptionByStoreId(storeId);
    }
	

}
