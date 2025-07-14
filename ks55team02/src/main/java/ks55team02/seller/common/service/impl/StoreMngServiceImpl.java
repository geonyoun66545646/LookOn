package ks55team02.seller.common.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ks55team02.seller.common.domain.Store;
import ks55team02.seller.common.domain.TopSellingProduct;
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
        Map<String, Object> store = storeMngMapper.getStoreSubscriptionByStoreId(storeId);
        
        return store;
    }
	
	@Override
	public Long getTotalSettleById(String storeID) {
		Long storeSettle = storeMngMapper.getTotalSettleById(storeID);
				
		return storeSettle;
	}
	
	@Override
	public Long getTotalOrderById(String storeID) {
		Long storeOrdCnt = storeMngMapper.getTotalOrderById(storeID);
		return storeOrdCnt;
	}
	
	@Override
	public Long getActGdsById(String storeId) {
		Long storeActGds = storeMngMapper.getActGdsById(storeId);
		return storeActGds;
	}
	
	@Override
	public List<TopSellingProduct> getTopSellingProductsByStoreId(String storeId) {
        List<TopSellingProduct> topSellingProducts = storeMngMapper.getTopSellingProductsByStoreId(storeId);
        log.info("MyBatis Mapper가 반환한 TopSellingProduct 리스트 크기: {}", topSellingProducts.size()); // 이 줄을 추가하세요.
        return topSellingProducts;
    }

}
