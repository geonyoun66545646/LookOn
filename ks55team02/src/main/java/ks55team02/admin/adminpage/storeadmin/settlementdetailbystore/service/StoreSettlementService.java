package ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreAccountDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementListViewDTO;
import ks55team02.admin.common.domain.SearchCriteria; // SearchCriteria 임포트

@Service
public interface StoreSettlementService {
    /**
     * 모든 상점의 정산 정보 목록을 조회합니다. (리스트 화면용)
     * 페이지네이션과 검색 조건을 적용합니다.
     * @param searchCriteria 검색 및 페이지네이션 조건
     * @return StoreSettlementListViewDTO 목록
     */
    List<StoreSettlementListViewDTO> getAllStoreSettlementsForList(SearchCriteria searchCriteria);

    /**
     * 전체 정산 정보의 개수를 조회합니다. (페이지네이션을 위해 필요)
     * @param searchCriteria 검색 조건 (전체 개수 조회 시 필터링을 위해 필요)
     * @return 전체 정산 정보 개수
     */
    int getTotalSettlementCount(SearchCriteria searchCriteria); // SearchCriteria 파라미터 추가

    /**
     * 특정 상점의 정산 내역을 조회합니다.
     * @param storeId 조회할 상점 ID
     * @return StoreSettlement 목록
     */
    List<StoreSettlementDTO> getSettlementHistoryByStoreId(String storeId);

    /**
     * 새로운 정산 대기 항목을 생성합니다.
     * @param storeId 상점 ID
     * @param totSelAmt 총 판매 금액
     * @param selFeeRt 판매 수수료율
     * @param plcyId 정책 ID
     * @return 성공 여부
     */
    boolean createPendingSettlement(String storeId, BigDecimal totSelAmt, BigDecimal selFeeRt, String plcyId);

    /**
     * 특정 정산 건의 상태를 '판매정산완료'로 업데이트합니다.
     * @param storeClclnId 정산할 정산 ID
     * @return 정산 성공 여부
     */
    boolean completeSettlement(String storeClclnId);

    /**
     * 선택된 여러 정산 건의 상태를 '판매정산완료'로 일괄 업데이트합니다.
     * @param storeClclnIds 정산할 정산 ID 목록
     * @return 일괄 정산 성공 여부 (모두 성공 시 true, 하나라도 실패 시 false)
     */
    boolean completeBatchSettlements(List<String> storeClclnIds);

    /**
     * 특정 정산 건에 대한 상세 정보를 조회합니다.
     * @param storeClclnId 조회할 정산 ID
     * @return StoreSettlementDTO 객체
     */
    StoreSettlementDTO getStoreSettlementById(String storeClclnId);

    /**
     * 특정 상점의 주 계좌 정보를 조회합니다.
     * @param storeId 상점 ID
     * @return StoreAccountDTO 객체
     */
    StoreAccountDTO getStoreAccountDetailsByStoreId(String storeId);
}