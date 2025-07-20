package ks55team02.admin.adminpage.storeadmin.appAdmin.service;

import java.util.List;

import ks55team02.admin.adminpage.storeadmin.appAdmin.domain.AppAdmin;
import ks55team02.common.domain.store.AppStore;

public interface AppAdminService {
	
	// 상점 신청 상세, 신청ID로 정보를 조회
	AppStore getAppAdminById(String aplyId);

	/**
     * 상점 신청 전체 개수 조회 (검색 조건 포함)
     * @param appAdmin 검색 조건을 담고 있는 AppAdmin 객체 (SearchCriteria 상속)
     * @return 전체 상점 신청 개수
     */
    int getAppAdminCount(AppStore appAdmin); // 메서드 구현 부분 제거

    /**
     * 상점 신청 목록 조회 (페이지네이션 및 검색 조건 포함)
     * @param appAdmin 페이지네이션 및 검색 조건을 담고 있는 AppAdmin 객체 (SearchCriteria 상속)
     * @return 상점 신청 목록
     */
    List<AppStore> getAppAdminList(AppStore appStore, int limitStart, int pageSize);
    
    // 상점 신청 상태 변경
    void updateAppStatus(AppStore appStore);

}