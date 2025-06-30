package ks55team02.customer.store.mapper;


import org.apache.ibatis.annotations.Mapper;

import ks55team02.customer.store.domain.StoreImage;

@Mapper
public interface StoreImageMapper {
    // 이미지 정보 저장
    int insertStoreImage(StoreImage storeImage);
    
    // 이미지 ID로 정보 조회 (필요시)
    StoreImage getStoreImageById(String imgId);
    
}