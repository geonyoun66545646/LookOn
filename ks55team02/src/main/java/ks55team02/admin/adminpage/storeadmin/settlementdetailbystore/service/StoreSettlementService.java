package ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementListViewDTO;

@Service
public interface StoreSettlementService {
	/**
     * 모든 상점의 정산 정보 목록을 조회합니다.
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
     * 특정 상점을 정산 처리합니다.
     * @param storeId 정산할 상점 ID
     * @param totSelAmt 총 판매 금액
     * @param selFeeRt 판매 수수료율
     * @param clclnAmt 정산 금액
     * @param actnoId 계좌 ID (실제로는 상점 정보에서 가져와야 함)
     * @return 정산 성공 여부
     */
    boolean processSettlement(String storeId, BigDecimal totSelAmt, BigDecimal selFeeRt, BigDecimal clclnAmt, String actnoId);

    /**
     * 특정 정산 내역의 상태를 업데이트합니다. (예: '정산대기' -> '정산완료')
     * @param storeClclnId 정산 ID
     * @param clclnSttsCd 변경할 정산 상태 코드
     * @return 업데이트 성공 여부
     */
    boolean updateSettlementStatus(String storeClclnId, String clclnSttsCd);
}
