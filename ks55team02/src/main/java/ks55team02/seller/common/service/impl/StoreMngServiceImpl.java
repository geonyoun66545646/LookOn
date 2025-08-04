package ks55team02.seller.common.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import ks55team02.common.domain.store.StoreImage;
import ks55team02.seller.common.domain.Store;
import ks55team02.seller.common.domain.TopSellingProduct;
import ks55team02.seller.common.mapper.StoreMngMapper;
import ks55team02.seller.common.service.StoreMngService;
import ks55team02.util.FileDetail;
import ks55team02.util.FilesUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class StoreMngServiceImpl implements StoreMngService{
	
	private final StoreMngMapper storeMngMapper;
	private final FilesUtils filesUtils;
	
	/*
	 * 
	 * @Override public List<Store> getSotreList() { // TODO Auto-generated method
	 * stub return null; }
	 */
	
	@Override
	public String getStoreIdBySellerId(String sellerId) {
	    return storeMngMapper.getStoreIdBySellerId(sellerId); // ⭐ 이 메서드를 추가합니다.
	}
	
	@Override
	public String getStoreIdBySellerLgnId(String sellerId) {
		return storeMngMapper.getStoreIdBySellerLgnId(sellerId);
	}
	
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
	
	@Override
    @Transactional // 중요: 이 메소드는 하나의 트랜잭션으로 처리됩니다.
    public void updateStoreLogo(String userlgnId, MultipartFile logoFile) {
        if (logoFile == null || logoFile.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 없습니다.");
        }

        String storeId = storeMngMapper.getStoreIdBySellerLgnId(userlgnId);
        
        // 1. 기존 로고 정보 조회 (삭제 처리를 위해)
        Store existingStore = storeMngMapper.getStoreInfoById(storeId);
        if (existingStore == null) {
            throw new RuntimeException("존재하지 않는 상점입니다.");
        }
        
        StoreImage oldLogo = existingStore.getLogo();

        // 2. 새 파일 저장 (FilesUtils 사용)
        FileDetail newFileDetail = filesUtils.saveFile(logoFile, "store-logos");
        
        // 3. 새 이미지 정보를 DB에 INSERT (Builder 패턴 사용)
        String newImgId = "store_img_" + UUID.randomUUID().toString().substring(0, 8);
        StoreImage newStoreImage = StoreImage.builder()
                .imgId(newImgId)
                .imgAddr(newFileDetail.getSavedPath())
                .imgFileNm(newFileDetail.getOriginalFileName())
                .imgFileSz(newFileDetail.getFileSize())
                .imgTypeCd(newFileDetail.getFileExtension())
                .regYmd(LocalDateTime.now())
                .delYn(false)
                .build();
        storeMngMapper.insertStoreImage(newStoreImage);
        
        // 4. stores 테이블의 로고 참조 업데이트
        Map<String, Object> params = new HashMap<>();
        params.put("storeId", storeId);
        params.put("newLogoId", newImgId);
        storeMngMapper.updateStoreLogoReference(params);
        
        // 5. 기존 로고가 있었다면 파일 시스템에서 삭제하고 DB에서 논리적 삭제 처리
        if (oldLogo != null && oldLogo.getImgId() != null) {
            storeMngMapper.markImageAsDeleted(oldLogo.getImgId());
            filesUtils.deleteFile(oldLogo.getImgAddr());
        }
    }

    @Override
    @Transactional
    public void updateStoreIntro(String storeId, String storeIntro) {
        Store store = new Store();
        store.setStoreId(storeId);
        store.setStoreIntroCn(storeIntro);
        storeMngMapper.updateStoreIntro(store);
    }

}
