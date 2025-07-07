package ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementListViewDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.mapper.StoreSettlementMapper;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.service.StoreSettlementService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional // 트랜잭션 관리
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
public class StoreSettlementServiceImpl implements StoreSettlementService {

    private final StoreSettlementMapper storeSettlementMapper;

    /**
     * 모든 상점의 정산 정보 목록을 조회합니다.
     * @return StoreSettlementListViewDTO 목록
     */
    @Override // 인터페이스 메소드 오버라이드
    public List<StoreSettlementListViewDTO> getAllStoreSettlementsForList() {
        return storeSettlementMapper.getAllStoreSettlementsForList();
    }

    /**
     * 특정 상점의 정산 내역을 조회합니다.
     * @param storeId 조회할 상점 ID
     * @return StoreSettlement 목록
     */
    @Override // 인터페이스 메소드 오버라이드
    public List<StoreSettlementDTO> getSettlementHistoryByStoreId(String storeId) {
        return storeSettlementMapper.getSettlementHistoryByStoreId(storeId);
    }

    /**
     * 특정 상점을 정산 처리합니다.
     * @param storeId 정산할 상점 ID
     * @param totSelAmt 총 판매 금액
     * @param selFeeRt 판매 수수료율
     * @param clclnAmt 정산 금액
     * @param actnoId 계좌 ID (실제로는 상점 정보에서 가져와야 함)
     * @return 정산 성공 여부
     */
    @Override // 인터페이스 메소드 오버라이드
    public boolean processSettlement(String storeId, BigDecimal totSelAmt, BigDecimal selFeeRt, BigDecimal clclnAmt, String actnoId) {
        try {
            // 다음 정산 ID 생성
            String nextClclnId = storeSettlementMapper.getNextStoreClclnId();

            StoreSettlementDTO newSettlement = new StoreSettlementDTO();
            newSettlement.setStoreClclnId(nextClclnId);
            newSettlement.setStoreId(storeId);
            newSettlement.setPlcyId("default_policy_id"); // 실제 정책 ID로 변경 필요
            newSettlement.setTotSelAmt(totSelAmt);
            newSettlement.setSelFeeRt(selFeeRt);
            newSettlement.setClclnAmt(clclnAmt);
            newSettlement.setActnoId(actnoId);
            newSettlement.setClclnPrcsYmd(LocalDate.now()); // 오늘 날짜로 정산 처리일 설정
            newSettlement.setClclnSttsCd("CLCL_COMPLETED"); // 정산 완료 상태 코드

            int result = storeSettlementMapper.insertStoreSettlement(newSettlement);
            return result > 0;
        } catch (Exception e) {
            System.err.println("정산 처리 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("정산 처리 실패", e);
        }
    }

    /**
     * 특정 정산 내역의 상태를 업데이트합니다. (예: '정산대기' -> '정산완료')
     * @param storeClclnId 정산 ID
     * @param clclnSttsCd 변경할 정산 상태 코드
     * @return 업데이트 성공 여부
     */
    @Override // 인터페이스 메소드 오버라이드
    public boolean updateSettlementStatus(String storeClclnId, String clclnSttsCd) {
        try {
            int result = storeSettlementMapper.updateSettlementStatus(storeClclnId, clclnSttsCd);
            return result > 0;
        } catch (Exception e) {
            System.err.println("정산 상태 업데이트 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("정산 상태 업데이트 실패", e);
        }
    }
}
