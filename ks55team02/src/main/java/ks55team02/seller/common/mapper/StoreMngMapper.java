package ks55team02.seller.common.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.common.domain.store.StoreImage;

import ks55team02.seller.common.domain.Store;
import ks55team02.seller.common.domain.TopSellingProduct;

@Mapper
public interface StoreMngMapper {

	// getStoreList 메서드가 데이터베이스에서 여러 개의 상점 정보를 조회한 후,
	// 그 각각의 상점 정보를 Store 객체 형태로 담아서 리스트(List)로 반환하겠다는 의미입니다.
	// List<store> getStoreList();

	String getStoreIdBySellerId(String sellerId); // ⭐ 이 메서드를 추가합니다.

	// #{storeId} @param으로 받음
	Store getStoreInfoById(@Param("storeId") String storeId);

	// 새로 추가된 메서드 (요약된 구독 기간)
	Map<String, Object> getStoreSubscriptionByStoreId(String storeId);

	// #{storeId} @param으로 받음
	Long getTotalSettleById(@Param("storeId") String storeId);

	Long getTotalOrderById(@Param("storeId") String storeId);

	Long getActGdsById(@Param("storeId") String storeId);

	// 상점별 판매 랭킹
	List<TopSellingProduct> getTopSellingProductsByStoreId(@Param("storeId") String storeId);
	
	int insertStoreImage(StoreImage storeImage);
	
    int updateStoreLogoReference(Map<String, Object> params);
    
    int updateStoreIntro(Store store);
    
    int markImageAsDeleted(String imgId);
    
}
