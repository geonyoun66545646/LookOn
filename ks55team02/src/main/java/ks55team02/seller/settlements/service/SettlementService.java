package ks55team02.seller.settlements.service;

import java.util.List;
import java.util.Map;

import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.seller.settlements.domain.SalesSttsDTO;
import ks55team02.seller.settlements.domain.SettlementSearchCriteria;

public interface SettlementService {

	 Map<String, Object> getMySettlementList(SearchCriteria criteria);

    String getStoreIdByUserNo(String userNo);

    /**
     * 특정 상점의 판매 내역을 조회하는 비즈니스 로직
     * @param storeId 상점 ID
     * @return 판매 내역 DTO 목록
     */
    List<SalesSttsDTO> getSalesHistoryByStoreId(String storeId); // 이 메서드도 유지합니다.

    /**
     * 모든 상점의 전체 판매 내역을 조회합니다.
     * @return 전체 판매 내역 DTO 목록
     */
    List<SalesSttsDTO> getAllSalesHistory();

    /**
     * 특정 상점의 판매 내역 전체 개수를 조회합니다. (검색/필터 조건 포함)
     * @param criteria 검색 및 페이지네이션 조건 (storeId 포함)
     * @return 판매 내역의 총 개수
     */
    int getSalesHistoryCountByStoreId(SettlementSearchCriteria criteria);

    /**
     * 페이지네이션 적용된 특정 상점의 판매 내역 리스트를 조회합니다. (검색/필터 조건, OFFSET/LIMIT 포함)
     * @param criteria 검색 및 페이지네이션 조건 (storeId, offset, pageSize 포함)
     * @return 조회된 SalesSttsDTO 리스트
     */
    List<SalesSttsDTO> getSalesHistoryByStoreIdAndPagination(SettlementSearchCriteria criteria);
}