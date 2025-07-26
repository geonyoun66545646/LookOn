package ks55team02.admin.adminpage.storeadmin.storemngadmin.service;

import java.util.List;
import ks55team02.common.domain.store.Store; // Store 클래스 임포트 확인

public interface StoreMngAdminService {

    /**
     * 상점 목록 전체 개수 조회 (검색 조건 포함)
     * @param store 검색 조건을 담고 있는 Store 객체 (SearchCriteria 상속 가정)
     * @return 전체 상점 개수
     */
    int getStoreCount(Store store);

    /**
     * 상점 목록 조회 (페이지네이션 및 검색 조건 포함)
     * @param store 페이지네이션 및 검색 조건을 담고 있는 Store 객체 (SearchCriteria 상속 가정)
     * @param limitStart LIMIT 구문의 시작 인덱스
     * @param pageSize LIMIT 구문의 가져올 레코드 개수
     * @return 상점 목록
     */
    List<Store> getStoreList(Store store, int limitStart, int pageSize);
    
    void updateStoreStatus(List<String> storeIds, String newStatus);
    
}