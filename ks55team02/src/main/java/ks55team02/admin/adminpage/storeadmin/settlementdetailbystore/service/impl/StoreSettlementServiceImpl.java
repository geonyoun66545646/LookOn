package ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreAccountDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementListViewDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.mapper.StoreSettlementMapper;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.service.StoreSettlementService;
import ks55team02.admin.common.domain.SearchCriteria; // SearchCriteria 추가

import lombok.RequiredArgsConstructor;

@Service
@Transactional // 트랜잭션 관리
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
public class StoreSettlementServiceImpl implements StoreSettlementService {

	private final StoreSettlementMapper storeSettlementMapper;

    /**
     * 전체 정산 정보의 개수를 조회합니다. (페이지네이션을 위해 필요)
     * @return 전체 정산 정보 개수
     */
    @Override
    public int getTotalSettlementCount() {
        return storeSettlementMapper.getTotalSettlementCount();
    }

    /**
     * 모든 상점의 정산 정보 목록을 조회합니다. (리스트 화면용)
     * @param searchCriteria 검색 및 페이지네이션 조건
     * @return StoreSettlementListViewDTO 목록
     */
    @Override
    public List<StoreSettlementListViewDTO> getAllStoreSettlementsForList(SearchCriteria searchCriteria) {
        // SearchCriteria 객체에서 offset과 pageSize를 가져와 매퍼에 전달합니다.
        return storeSettlementMapper.getAllStoreSettlementsForList(searchCriteria.getOffset(), searchCriteria.getPageSize());
    }

    /**
     * 특정 상점의 정산 내역을 조회합니다.
     * @param storeId 조회할 상점 ID
     * @return StoreSettlement 목록
     */
    @Override
    public List<StoreSettlementDTO> getSettlementHistoryByStoreId(String storeId) {
        return storeSettlementMapper.getSettlementHistoryByStoreId(storeId);
    }

    /**
     * 특정 정산 건의 상태를 '판매정산완료'로 업데이트합니다.
     * @param storeClclnId 정산할 정산 ID
     * @return 정산 성공 여부
     */
    @Override
    public boolean completeSettlement(String storeClclnId) {
        return storeSettlementMapper.updateSettlementStatus(storeClclnId, "판매정산완료") > 0;
    }

    /**
     * 선택된 여러 정산 건의 상태를 '판매정산완료'로 일괄 업데이트합니다.
     * @param storeClclnIds 정산할 정산 ID 목록
     * @return 일괄 정산 성공 여부 (모두 성공 시 true, 하나라도 실패 시 false)
     */
    @Override
    public boolean completeBatchSettlements(List<String> storeClclnIds) {
        if (storeClclnIds == null || storeClclnIds.isEmpty()) {
            return false;
        }
        return storeSettlementMapper.updateSettlementStatusBatch(storeClclnIds, "판매정산완료") > 0;
    }

    /**
     * 새로운 정산 대기 항목을 생성합니다.
     * @param storeId 상점 ID
     * @param totSelAmt 총 판매 금액
     * @param selFeeRt 판매 수수료율 (정수 형태)
     * @param plcyId 정책 ID
     * @return 성공 여부
     */
    @Override
    public boolean createPendingSettlement(String storeId, BigDecimal totSelAmt, BigDecimal selFeeRt, String plcyId) {
        try {
            // 다음 정산 ID 생성
            Integer maxSeq = storeSettlementMapper.getMaxStoreClclnSeq();
            String nextClclnId = "clcln_" + String.format("%05d", maxSeq + 1);

            // 수수료 금액 계산: 총 판매액 * (수수료율 / 100) -> 반올림
            // selFeeRt는 이미 BigDecimal 형태의 비율 (예: 0.05)이라고 가정
            BigDecimal commissionAmount = totSelAmt.multiply(selFeeRt).setScale(0, RoundingMode.HALF_UP);

            // 정산 금액 계산: 총 판매액 - 수수료 금액
            BigDecimal clclnAmt = totSelAmt.subtract(commissionAmount);

            // StoreSettlementDTO 생성 및 값 설정
            StoreSettlementDTO newSettlement = new StoreSettlementDTO();
            newSettlement.setStoreClclnId(nextClclnId);
            newSettlement.setStoreId(storeId);
            newSettlement.setPlcyId(plcyId);
            newSettlement.setTotSelAmt(totSelAmt);
            newSettlement.setSelFeeRt(selFeeRt); // 소수점 형태의 수수료율 저장
            newSettlement.setClclnAmt(clclnAmt);
            newSettlement.setActnoId(null); // 계좌 ID는 초기에는 null 또는 빈 값으로 설정 (정산 처리 시 업데이트)
            newSettlement.setClclnPrcsYmd(null); // 정산 대기이므로 처리일은 NULL
            newSettlement.setClclnSttsCd("판매정산대기"); // 정산 대기 상태 코드

            // DB에 삽입
            int result = storeSettlementMapper.insertStoreSettlement(newSettlement);
            return result > 0;

        } catch (Exception e) {
            System.err.println("정산 대기 항목 생성 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("정산 대기 항목 생성 실패", e);
        }
    }

    /**
     * 특정 상점의 계좌 정보를 조회합니다.
     * @param storeId 상점 ID
     * @return StoreAccountDTO 객체
     */
    @Override
    public StoreAccountDTO getStoreAccountDetailsByStoreId(String storeId) {
        return storeSettlementMapper.getStoreAccountDetailsByStoreId(storeId);
    }
}