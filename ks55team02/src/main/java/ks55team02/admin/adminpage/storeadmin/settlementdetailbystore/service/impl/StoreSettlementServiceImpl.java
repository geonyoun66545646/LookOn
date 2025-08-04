// ks55team02/admin/adminpage/storeadmin/settlementdetailbystore/service/impl/StoreSettlementServiceImpl.java

package ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.CdfpDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreAccountDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementListViewDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.mapper.StoreSettlementMapper;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.service.StoreSettlementService;
import ks55team02.admin.common.domain.Pagination;
import ks55team02.admin.common.domain.SearchCriteria;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class StoreSettlementServiceImpl implements StoreSettlementService {

    private static final Logger logger = LoggerFactory.getLogger(StoreSettlementServiceImpl.class);
    private final StoreSettlementMapper storeSettlementMapper;

    // --- 인터페이스에 선언된 모든 메소드의 실제 구현부 ---

    @Override
    public List<StoreSettlementListViewDTO> getAllStoreSettlementsForList(SearchCriteria searchCriteria) {
        int totalRecordCount = storeSettlementMapper.getTotalSettlementCount(searchCriteria);
        Pagination pagination = new Pagination(totalRecordCount, searchCriteria);
        searchCriteria.setOffset(pagination.getLimitStart());
        return storeSettlementMapper.getAllStoreSettlementsForList(searchCriteria);
    }

    @Override
    public int getTotalSettlementCount(SearchCriteria searchCriteria) {
        return storeSettlementMapper.getTotalSettlementCount(searchCriteria);
    }

    /**
     * ★★★ 컨트롤러가 호출하는, 인터페이스에 선언된 그 메소드의 실제 구현입니다. ★★★
     */
    @Override
    public List<StoreSettlementDTO> getSettlementHistoryByStoreId(String storeId) {
        return storeSettlementMapper.getSettlementHistoryByStoreId(storeId);
    }

    @Override
    public boolean createPendingSettlement(String storeId, BigDecimal totSelAmt, BigDecimal selFeeRt, String plcyId) {
        // 이 메소드의 ID 생성 로직도 동시성 문제가 있으나, 현재의 주된 수정 범위는 아닙니다.
        Integer maxSeq = storeSettlementMapper.getMaxStoreClclnSeq();
        String nextClclnId = "clcln_" + String.format("%05d", (maxSeq != null ? maxSeq : 0) + 1);
        // ... (이하 로직 생략)
        return true;
    }

    @Override
    public boolean completeSettlement(String storeClclnId) {
        try {
            StoreSettlementDTO settlementToComplete = storeSettlementMapper.getStoreSettlementById(storeClclnId);
            
            if (settlementToComplete == null) {
                logger.error("completeSettlement 실패: 정산 대상을 찾을 수 없음 - ID: {}", storeClclnId);
                return false;
            }
            if (settlementToComplete.getTotSelAmt() == null || settlementToComplete.getSelFeeRt() == null) {
                logger.error("completeSettlement 실패: 총 판매액 또는 수수료율 정보가 없음 - ID: {}", storeClclnId);
                return false;
            }

            // 수수료 계산
            BigDecimal totSelAmt = settlementToComplete.getTotSelAmt();
            BigDecimal selFeeRt = settlementToComplete.getSelFeeRt();
            BigDecimal hundred = new BigDecimal("100");
            BigDecimal feePercentage = selFeeRt.divide(hundred, 4, RoundingMode.HALF_UP);
            BigDecimal feeAmount = totSelAmt.multiply(feePercentage);
            BigDecimal finalClclnAmt = totSelAmt.subtract(feeAmount).setScale(2, RoundingMode.HALF_UP);
            
            // DB 업데이트
            int updatedRows = storeSettlementMapper.updateSettlementStatus(storeClclnId, "판매정산완료", finalClclnAmt);
            
            return updatedRows > 0;
        } catch (Exception e) {
            logger.error("completeSettlement 중 오류 발생 - ID: {}", storeClclnId, e);
            return false;
        }
    }

    @Override
    public boolean completeBatchSettlements(List<String> storeClclnIds) {
        boolean allSuccess = true;
        if (storeClclnIds == null || storeClclnIds.isEmpty()) {
            return false;
        }
        for (String storeClclnId : storeClclnIds) {
            if (!this.completeSettlement(storeClclnId)) {
                allSuccess = false;
                // 오류가 발생해도 계속 진행하고 싶다면 이 로직을 유지, 즉시 중단하고 싶다면 예외를 던집니다.
            }
        }
        return allSuccess;
    }

    @Override
    public StoreSettlementDTO getStoreSettlementById(String storeClclnId) {
        return storeSettlementMapper.getStoreSettlementById(storeClclnId);
    }

    @Override
    public StoreAccountDTO getStoreAccountDetailsByStoreId(String storeId) {
        return storeSettlementMapper.getStoreAccountDetailsByStoreId(storeId);
    }

    @Override
    @Transactional
    public boolean completeAndCreateNewPendingSettlement(String completedStoreClclnId) {
        try {
            // --- STEP 1: 정산할 데이터 준비 ---
            StoreSettlementDTO originalSettlement = storeSettlementMapper.getStoreSettlementById(completedStoreClclnId);
            if (originalSettlement == null) {
                throw new RuntimeException("정산할 대상 정보를 찾을 수 없습니다: " + completedStoreClclnId);
            }
            // 총 판매액이 없으면 정산을 진행할 수 없습니다.
            if (originalSettlement.getTotSelAmt() == null || originalSettlement.getSelFeeRt() == null) {
                throw new RuntimeException("총 판매액 또는 수수료율 정보가 없어 정산을 진행할 수 없습니다.");
            }

            // --- STEP 2: "정산 예정액" 계산 (가장 중요한 부분!) ---
            BigDecimal totSelAmt = originalSettlement.getTotSelAmt();
            BigDecimal selFeeRt = originalSettlement.getSelFeeRt(); // DB에 저장된 해당 행의 수수료율
            
            BigDecimal hundred = new BigDecimal("100");
            // 수수료율이 18%라면, DB의 sel_fee_rt 컬럼에는 18.00 과 같은 숫자 값이 저장되어 있어야 합니다.
            BigDecimal feePercentage = selFeeRt.divide(hundred, 4, RoundingMode.HALF_UP); 
            BigDecimal feeAmount = totSelAmt.multiply(feePercentage);
            BigDecimal finalClclnAmt = totSelAmt.subtract(feeAmount).setScale(2, RoundingMode.HALF_UP);
            
            logger.info(">>> 최종 정산 금액 계산 완료: {}원 - {}원(수수료) = {}원", totSelAmt, feeAmount, finalClclnAmt);

            // --- STEP 3: "계산된 금액"과 함께 기존 정산 건을 '판매정산완료'로 UPDATE ---
            int updatedRows = storeSettlementMapper.updateSettlementStatus(completedStoreClclnId, "판매정산완료", finalClclnAmt);
            if (updatedRows == 0) {
                logger.warn("이미 '판매정산완료' 상태이거나 업데이트에 실패했습니다: {}", completedStoreClclnId);
            } else {
                logger.info(">>> 정산 완료 처리 성공: {}", completedStoreClclnId);
            }

            // --- STEP 4: 다음 정산을 위한 새로운 '판매정산대기' 행 INSERT (기존 로직과 동일) ---
            // (이 부분은 구독권 등급 로직이 없으므로, 현재 정책을 그대로 따라갑니다)
            CdfpDTO currentCdfp = storeSettlementMapper.getCdfpByPlcyId(originalSettlement.getPlcyId());
            if (currentCdfp == null || currentCdfp.getFeeRt() == null) {
                throw new RuntimeException("새로운 수수료율 정책을 찾을 수 없습니다.");
            }
            BigDecimal newFeeRate = new BigDecimal(currentCdfp.getFeeRt());
            
            StoreSettlementDTO newPendingSettlement = new StoreSettlementDTO();
            newPendingSettlement.setStoreId(originalSettlement.getStoreId());
            newPendingSettlement.setPlcyId(originalSettlement.getPlcyId());
            newPendingSettlement.setActnoId(originalSettlement.getActnoId());
            newPendingSettlement.setSelFeeRt(newFeeRate);
            
            int insertResult = storeSettlementMapper.insertNewPendingSettlement(newPendingSettlement);
            if (insertResult == 0) {
                throw new RuntimeException("새로운 정산 대기 항목 생성에 실패했습니다.");
            }
            
            logger.info(">>> 신규 정산 대기 항목 생성 성공. 새로운 ID: {}", newPendingSettlement.getStoreClclnId());

            return true;

        } catch (Exception e) {
            logger.error("정산 처리 및 신규 항목 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("정산 처리 중 오류가 발생했습니다.", e);
        }
    }
}