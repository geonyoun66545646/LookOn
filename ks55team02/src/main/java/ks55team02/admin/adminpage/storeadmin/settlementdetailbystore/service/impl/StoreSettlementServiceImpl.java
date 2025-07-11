package ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.CdfpDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreAccountDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementListViewDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.mapper.StoreSettlementMapper;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.service.StoreSettlementService;
import ks55team02.admin.common.domain.Pagination; // Pagination 추가
import ks55team02.admin.common.domain.SearchCriteria; // SearchCriteria 추가

import lombok.RequiredArgsConstructor;

@Service
@Transactional // 트랜잭션 관리
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
public class StoreSettlementServiceImpl implements StoreSettlementService {

    private final StoreSettlementMapper storeSettlementMapper;

    /**
     * 전체 정산 정보의 개수를 조회합니다. (페이지네이션을 위해 필요)
     * @param searchCriteria 검색 조건 (전체 개수 조회 시 필터링을 위해 필요)
     * @return 전체 정산 정보 개수
     */
    @Override
    public int getTotalSettlementCount(SearchCriteria searchCriteria) {
        return storeSettlementMapper.getTotalSettlementCount(searchCriteria); // searchCriteria 전달
    }

    /**
     * 모든 상점의 정산 정보 목록을 조회합니다. (리스트 화면용)
     * 페이지네이션과 검색 조건을 적용합니다.
     * @param searchCriteria 검색 및 페이지네이션 조건
     * @return StoreSettlementListViewDTO 목록
     */
    @Override
    public List<StoreSettlementListViewDTO> getAllStoreSettlementsForList(SearchCriteria searchCriteria) {
        // 1. 전체 레코드 개수를 먼저 조회 (검색 조건 적용)
        int totalRecordCount = storeSettlementMapper.getTotalSettlementCount(searchCriteria);

        // 2. Pagination 객체 생성 및 offset 계산
        Pagination pagination = new Pagination(totalRecordCount, searchCriteria);
        searchCriteria.setOffset(pagination.getLimitStart()); // SearchCriteria에 offset 설정

        // 3. 매퍼 호출 (searchCriteria 객체 전체 전달)
        return storeSettlementMapper.getAllStoreSettlementsForList(searchCriteria);
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
     * 새로운 정산 대기 항목을 생성합니다.
     * @param storeId 상점 ID
     * @param totSelAmt 총 판매 금액
     * @param selFeeRt 판매 수수료율
     * @param plcyId 정책 ID
     * @return 성공 여부
     */
    @Override
    public boolean createPendingSettlement(String storeId, BigDecimal totSelAmt, BigDecimal selFeeRt, String plcyId) {
        try {
            // 1. 새로운 storeClclnId 생성 (예: clcln_00001)
            Integer maxSeq = storeSettlementMapper.getMaxStoreClclnSeq();
            String nextClclnId = "clcln_" + String.format("%05d", (maxSeq != null ? maxSeq : 0) + 1); // null 체크 추가

            // 2. 상점의 주 계좌 정보 조회
            StoreAccountDTO storeAccount = storeSettlementMapper.getStoreAccountDetailsByStoreId(storeId);
            String actnoId = (storeAccount != null) ? storeAccount.getActnoId() : null; // 계좌 ID 가져오기

            // 3. 수수료율 조회 (cdfp 테이블에서) - 필요 시, 현재는 파라미터로 받은 selFeeRt 사용
            // CdfpDTO cdfp = storeSettlementMapper.getCdfpByPlcyId(plcyId);

            // 4. 수수료 금액 계산: 총 판매액 * 판매 수수료율
            BigDecimal commissionAmount = totSelAmt.multiply(selFeeRt).setScale(0, RoundingMode.HALF_UP);

            // 5. 정산 금액 계산: 총 판매액 - 수수료 금액
            BigDecimal clclnAmt = totSelAmt.subtract(commissionAmount);

            // 6. StoreSettlementDTO 생성 및 값 설정
            StoreSettlementDTO newSettlement = new StoreSettlementDTO();
            newSettlement.setStoreClclnId(nextClclnId);
            newSettlement.setStoreId(storeId);
            newSettlement.setPlcyId(plcyId);
            newSettlement.setTotSelAmt(totSelAmt);
            newSettlement.setSelFeeRt(selFeeRt); // 소수점 형태의 수수료율 저장
            newSettlement.setClclnAmt(clclnAmt);
            newSettlement.setActnoId(actnoId); // 계좌 ID 설정
            newSettlement.setClclnPrcsYmd(null); // 정산 대기이므로 처리일은 NULL
            newSettlement.setClclnSttsCd("판매정산대기"); // 정산 대기 상태 코드

            // 7. DB에 삽입
            int result = storeSettlementMapper.insertStoreSettlement(newSettlement);
            return result > 0;

        } catch (Exception e) {
            System.err.println("정산 대기 항목 생성 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("정산 대기 항목 생성 실패", e);
        }
    }

    /**
     * 특정 정산 건의 상태를 '판매정산완료'로 업데이트합니다.
     * @param storeClclnId 정산할 정산 ID
     * @return 정산 성공 여부
     */
    @Override
    public boolean completeSettlement(String storeClclnId) {
        try {
            int updatedRows = storeSettlementMapper.updateSettlementStatus(storeClclnId, "판매정산완료");
            return updatedRows > 0;
        } catch (Exception e) {
            System.err.println("정산 완료 처리 중 오류 발생: " + e.getMessage());
            return false;
        }
    }

    /**
     * 선택된 여러 정산 건의 상태를 '판매정산완료'로 일괄 업데이트합니다.
     * @param storeClclnIds 정산할 정산 ID 목록
     * @return 일괄 정산 성공 여부 (모두 성공 시 true, 하나라도 실패 시 false)
     */
    @Override
    public boolean completeBatchSettlements(List<String> storeClclnIds) {
        boolean allSuccess = true;
        if (storeClclnIds == null || storeClclnIds.isEmpty()) {
            return false;
        }
        for (String storeClclnId : storeClclnIds) {
            if (!completeSettlement(storeClclnId)) { // 개별 completeSettlement 호출 (updateSettlementStatusBatch 없음)
                allSuccess = false;
                System.err.println("정산 실패: " + storeClclnId);
            }
        }
        return allSuccess;
    }

    /**
     * 특정 정산 건에 대한 상세 정보를 조회합니다.
     * @param storeClclnId 조회할 정산 ID
     * @return StoreSettlementDTO 객체
     */
    @Override
    public StoreSettlementDTO getStoreSettlementById(String storeClclnId) {
        return storeSettlementMapper.getStoreSettlementById(storeClclnId);
    }

    /**
     * 특정 상점의 주 계좌 정보를 조회합니다.
     * @param storeId 상점 ID
     * @return StoreAccountDTO 객체
     */
    @Override
    public StoreAccountDTO getStoreAccountDetailsByStoreId(String storeId) {
        return storeSettlementMapper.getStoreAccountDetailsByStoreId(storeId);
    }
}