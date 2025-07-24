// ks55team02/admin/adminpage/storeadmin/settlementdetailbystore/service/StoreSettlementService.java

package ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.service;

import java.math.BigDecimal;
import java.util.List;

import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreAccountDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementListViewDTO;
import ks55team02.admin.common.domain.SearchCriteria;

/**
 * 정산 관련 비즈니스 로직을 위한 서비스 인터페이스 (설계도)
 * 컨트롤러는 반드시 여기에 선언된 메소드만 호출할 수 있습니다.
 */
public interface StoreSettlementService {

    // --- settlementDetailByStore.html (메인 목록)을 위한 메소드 ---
    List<StoreSettlementListViewDTO> getAllStoreSettlementsForList(SearchCriteria searchCriteria);
    int getTotalSettlementCount(SearchCriteria searchCriteria);


    // --- JavaScript API 호출을 위한 메소드들 ---

    /**
     * ★★★ 지적해주신, 누락되었던 바로 그 메소드 선언입니다. ★★★
     * 특정 상점의 정산 전체 내역을 조회합니다. (모달창에 표시용)
     * @param storeId 조회할 상점 ID
     * @return 해당 상점의 StoreSettlement 목록
     */
    List<StoreSettlementDTO> getSettlementHistoryByStoreId(String storeId);

    /**
     * 특정 상점의 주 계좌 정보를 조회합니다.
     * @param storeId 상점 ID
     * @return StoreAccountDTO 객체
     */
    StoreAccountDTO getStoreAccountDetailsByStoreId(String storeId);

    /**
     * 선택된 여러 정산 건을 일괄 처리합니다.
     * @param storeClclnIds 정산할 정산 ID 목록
     * @return 처리 성공 여부
     */
    boolean completeBatchSettlements(List<String> storeClclnIds);


    // --- 정산 처리 로직을 위한 메소드들 ---

    /**
     * 특정 정산 건의 상세 정보를 조회합니다.
     * ServiceImpl 내부에서 다음 정산 건 생성을 위해 사용됩니다.
     * @param storeClclnId 조회할 정산 ID
     * @return StoreSettlementDTO 객체
     */
    StoreSettlementDTO getStoreSettlementById(String storeClclnId);

    /**
     * 최종적으로 사용할 '정산 완료 및 다음 건 생성' 핵심 메소드입니다.
     * @param completedStoreClclnId 완료 처리할 기존 정산 건의 ID
     * @return 처리 성공 여부
     */
    boolean completeAndCreateNewPendingSettlement(String completedStoreClclnId);


    // --- 아래는 현재 메인 로직에서는 사용되지 않지만, 다른 곳에서 사용할 수 있는 메소드들 ---
    boolean createPendingSettlement(String storeId, BigDecimal totSelAmt, BigDecimal selFeeRt, String plcyId);
    boolean completeSettlement(String storeClclnId);
}