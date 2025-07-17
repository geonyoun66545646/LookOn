package ks55team02.customer.store.mapper;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.common.domain.store.AppStore;

@Mapper
public interface AppStoreMapper {
	
	AppStore getAppStoreByUserNo(String aplyUserNo);

}
