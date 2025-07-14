package ks55team02.customer.store.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.customer.store.domain.Store;



@Mapper
public interface CustomerStoreMapper {
	List<Store> getAllStores();

}
