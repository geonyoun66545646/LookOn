package ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementListViewDTO;

@Mapper
public interface StoreSettlementMapper {
	/**
     * 모든 상점의 정산 정보 목록을 조회합니다. (리스트 화면용)
     * 상점명(store_nm)을 포함하여 조회합니다.
     * @return StoreSettlementListViewDTO 목록
     */
    List<StoreSettlementListViewDTO> getAllStoreSettlementsForList();

    /**
     * 특정 상점의 정산 내역을 조회합니다.
     * @param storeId 조회할 상점 ID
     * @return StoreSettlement 목록
     */
    List<StoreSettlementDTO> getSettlementHistoryByStoreId(String storeId);

    /**
     * 새로운 정산 정보를 삽입합니다.
     * @param storeSettlement 삽입할 정산 정보
     * @return 삽입된 행의 수
     */
    int insertStoreSettlement(StoreSettlementDTO storeSettlement);

    /**
     * 다음 정산 ID를 조회합니다. (예: 'clcln_00001' 형식)
     * @return 다음 정산 ID 문자열
     */
    String getNextStoreClclnId();

    /**
     * 특정 정산 ID의 상태를 업데이트합니다.
     * @param storeClclnId 정산 ID
     * @param clclnSttsCd 변경할 정산 상태 코드
     * @return 업데이트된 행의 수
     */
    int updateSettlementStatus(String storeClclnId, String clclnSttsCd);
}
