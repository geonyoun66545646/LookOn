package ks55team02.seller.stores.mapper; // stores 패키지를 권장합니다.

import ks55team02.seller.stores.domain.Stores;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface StoreMapper {
	
	Stores getStoreById(String storeId);
	
	/**
     * 검색어와 일치하는 브랜드(상점) 목록을 조회합니다.
     * @param keyword 검색어
     * @return 검색된 브랜드 목록
     */
    List<Stores> searchStoresByKeyword(String keyword);
	
    /**
     * 모든 활성화된 브랜드(상점) 목록을 조회합니다.
     * @return 상점 목록
     */
    List<Stores> getAllStores();
}