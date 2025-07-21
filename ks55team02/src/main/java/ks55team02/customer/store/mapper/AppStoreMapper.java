package ks55team02.customer.store.mapper;

import org.apache.ibatis.annotations.Mapper;
import ks55team02.common.domain.store.AppStore;

@Mapper
public interface AppStoreMapper {
	
	AppStore getAppStoreByUserNo(String aplyUserNo);

    // 상점 신청 정보 삽입
    int addAppStore(AppStore appStore);

    // 가장 큰 신청 ID의 숫자 부분 조회
    Integer getMaxAplyIdNum(); // Integer로 반환하여 null 처리 가능
}
