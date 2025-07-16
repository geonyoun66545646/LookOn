package ks55team02.customer.store.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.common.domain.store.Store;



@Mapper
public interface CustomerStoreMapper {
	List<Store> getAllStores();

}
