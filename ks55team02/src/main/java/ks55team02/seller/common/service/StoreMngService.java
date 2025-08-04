package ks55team02.seller.common.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import ks55team02.seller.common.domain.Store;
import ks55team02.seller.common.domain.TopSellingProduct;

public interface StoreMngService {
	
	String getStoreIdBySellerId(String sellerId); // ⭐ 이 메서드를 추가합니다.

	String getStoreIdBySellerLgnId(String sellerId);
	
	// List<Store> getSotreList();
	Store getStoreInfoById(String storeId);
	
	// 특정 상점 ID에 대한 구독 기간 통합 정보를 조회합니다.
	Map<String, Object> getStoreSubscriptionByStoreId(String storeId);
	
	// 특정 상점 ID에 대한 총 판매금액을 조회합니다.
	Long getTotalSettleById(String storeID);
	
	// 특정 상점 ID에 대한 총 상품판매 개수를 조회 합니다.
	Long getTotalOrderById(String storeID);
	
	// 특정 상점 ID에 대한 총 활성화된 등록 상품 개수를 조회 합니다.
	Long getActGdsById(String storeId);
	
	// 특정 상점 ID에 대한 상점별 판매 탑 5 랭킹
	List<TopSellingProduct> getTopSellingProductsByStoreId(String storeId);
	
	void updateStoreLogo(String userlgnId, MultipartFile logoFile);
	
    void updateStoreIntro(String storeId, String storeIntro);
	
}