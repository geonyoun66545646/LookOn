package ks55team02.seller.settlements.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param; // @Param 어노테이션 사용 시 필요

import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.seller.settlements.domain.SalesSttsDTO;
import ks55team02.seller.settlements.domain.SettlementDTO;
import ks55team02.seller.settlements.domain.SettlementSearchCriteria;

@Mapper
public interface SettlementMapper {

	int countMySettlementList(SearchCriteria criteria);
    
    List<SettlementDTO> selectMySettlementList(SearchCriteria criteria);
    
    String selectStoreIdByUserNo(String userNo);

    /**
     * 특정 상점의 판매 내역을 조회하는 MyBatis 쿼리 매핑
     * @param storeId 상점 ID
     * @return 판매 내역 DTO 목록
     */
    List<SalesSttsDTO> getSalesHistoryByStoreId(@Param("storeId") String storeId);

    /**
     * 모든 상점의 전체 판매 내역을 조회하는 MyBatis 쿼리 매핑
     * @return 전체 판매 내역 DTO 목록
     */
    List<SalesSttsDTO> getAllSalesHistory();

    /**
     * 특정 상점의 판매 내역 전체 개수를 조회하는 MyBatis 쿼리 매핑 (검색/필터 조건 포함)
     * @param criteria 검색 및 페이지네이션 조건 (storeId 포함)
     * @return 판매 내역의 총 개수
     */
    int countSalesHistoryByStoreId(SettlementSearchCriteria criteria);

    /**
     * 페이지네이션 적용된 특정 상점의 판매 내역 리스트를 조회하는 MyBatis 쿼리 매핑
     * @param criteria 검색 및 페이지네이션 조건 (storeId, offset, pageSize 포함)
     * @return 조회된 SalesSttsDTO 리스트
     */
    List<SalesSttsDTO> selectSalesHistoryByStoreIdAndPagination(SettlementSearchCriteria criteria);
}