package ks55team02.seller.premiumcheckout.service.impl;

import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.seller.premiumcheckout.domain.SellerPremiumDTO;
import ks55team02.seller.premiumcheckout.domain.SellerPremiumPaymentDTO;
import ks55team02.seller.premiumcheckout.mapper.SellerPremiumMapper;
import ks55team02.seller.premiumcheckout.service.SellerPremiumService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// import java.util.UUID; // UUID는 더 이상 필요 없으므로 주석 처리 또는 삭제

@Service
@RequiredArgsConstructor
@Log4j2
public class SellerPremiumServiceImpl implements SellerPremiumService {

    private final SellerPremiumMapper sellerPremiumMapper;

    @Override
    public List<SellerPremiumDTO> getSubscriptionPlanList(SearchCriteria searchCriteria) {
        return sellerPremiumMapper.getSubscriptionPlanList(searchCriteria);
    }

    @Override
    public SellerPremiumDTO getSubscriptionPlanById(String sbscrPlanId) {
        return sellerPremiumMapper.getSubscriptionPlanById(sbscrPlanId);
    }

    @Override
    public int getSubscriptionPlanCount(SearchCriteria searchCriteria) {
        return sellerPremiumMapper.getSubscriptionPlanCount(searchCriteria);
    }

    @Override
    @Transactional
    public Map<String, Object> createPremiumOrder(Map<String, Object> payload, String userNo, String userName) {
        log.info("createPremiumOrder 메서드 시작. payload: {}, userNo: {}, userName: {}", payload, userNo, userName);

        String planId = (String) payload.get("planId");
        String planName = (String) payload.get("planName");
        Integer quantity = (Integer) payload.get("quantity");
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());

        // ----------------------------------------------------------------------
        // 주문 번호 생성 로직 변경
        Integer maxSequence = sellerPremiumMapper.getMaxSbscrStlmIdSequence();
        long nextSequence;

        if (maxSequence == null || maxSequence < 50) { // 기존 ID가 없거나 50보다 작은 경우 50부터 시작
            nextSequence = 50;
        } else {
            nextSequence = maxSequence + 1;
        }
        String orderId = "subpay_sell_" + nextSequence;
        log.info(">>>>>> 새로 생성된 주문 ID: {}", orderId);
        // ----------------------------------------------------------------------

        SellerPremiumPaymentDTO paymentOrder = new SellerPremiumPaymentDTO();
        paymentOrder.setSbscrStlmId(orderId);
        paymentOrder.setUserNo(userNo);
        paymentOrder.setSbscrPlanId(planId);
        paymentOrder.setSbscrPrchsNocs(quantity);
        paymentOrder.setSbscrTotStlmAmt(amount);
        paymentOrder.setSbscrStlmSttsCd("PENDING");
        paymentOrder.setStlmDmndDt(LocalDateTime.now());

        sellerPremiumMapper.insertPremiumOrder(paymentOrder);
        log.info(">>>>>> DB에 주문 정보 삽입 성공: 주문 ID = {}", orderId);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("amount", amount);
        response.put("orderName", planName + " " + quantity + "개");
        response.put("customerName", userName);

        log.info(">>>>>> 주문 생성 성공. 응답: {}", response);
        return response;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SellerPremiumPaymentDTO processPaymentSuccess(String orderId, String paymentKey, BigDecimal amount, String userNo) {
        log.info("결제 성공 처리 시작 - orderId: {}, paymentKey: {}, amount: {}, userNo: {}", orderId, paymentKey, amount, userNo);

        SellerPremiumPaymentDTO paymentInfo = null;
        int retryCount = 0;
        final int MAX_RETRIES = 5;
        final long SLEEP_MILLIS = 1000;

        while (paymentInfo == null && retryCount < MAX_RETRIES) {
            retryCount++;
            log.warn("주문 ID {}에 대한 결제 정보 조회 시도 중 (시도 {}/{})", orderId, retryCount, MAX_RETRIES);
            paymentInfo = sellerPremiumMapper.findPaymentWithPlanNameByOrderId(orderId);
            if (paymentInfo == null) {
                try {
                    log.warn("주문 ID {}에 대한 결제 정보 조회 실패. {}초 후 재시도합니다.", orderId, SLEEP_MILLIS / 1000);
                    Thread.sleep(SLEEP_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("재시도 대기 중 인터럽트 발생: {}", e.getMessage(), e);
                    break;
                }
            }
        }

        if (paymentInfo == null) {
            log.error("최종적으로 주문 ID {}에 해당하는 결제 정보(paymentInfo)를 찾을 수 없습니다. 결제 처리 실패.", orderId);
            return null;
        }

        log.info("성공적으로 조회된 paymentInfo 객체 상세:");
        log.info("  sbscrStlmId: {}", paymentInfo.getSbscrStlmId());
        log.info("  userNo: {}", paymentInfo.getUserNo());
        log.info("  sbscrPlanId: {}", paymentInfo.getSbscrPlanId());
        log.info("  sbscrPrchsNocs: {}", paymentInfo.getSbscrPrchsNocs());
        log.info("  sbscrTotStlmAmt: {}", paymentInfo.getSbscrTotStlmAmt());
        log.info("  sbscrStlmMthdCd: {}", paymentInfo.getSbscrStlmMthdCd());
        log.info("  sbscrBgngDt: {}", paymentInfo.getSbscrBgngDt());
        log.info("  sbscrEndDt: {}", paymentInfo.getSbscrEndDt());
        log.info("  sbscrStlmSttsCd: {}", paymentInfo.getSbscrStlmSttsCd());
        log.info("  pgDlngId: {}", paymentInfo.getPgDlngId());
        log.info("  pgCoInfo: {}", paymentInfo.getPgCoInfo());
        log.info("  stlmCmptnDt: {}", paymentInfo.getStlmCmptnDt());
        log.info("  stlmDmndDt: {}", paymentInfo.getStlmDmndDt());
        log.info("  sbscrPlanNm: {}", paymentInfo.getSbscrPlanNm());


        int quantity = paymentInfo.getSbscrPrchsNocs();
        String planId = paymentInfo.getSbscrPlanId();
        
        int planTermDays = sellerPremiumMapper.findPlanTermById(planId);
        long totalSubscriptionDays = (long) planTermDays * quantity;
        
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(totalSubscriptionDays);

        sellerPremiumMapper.updatePaymentSuccess(orderId, paymentKey, startDate, endDate);
        log.info("subscription_payments 테이블 업데이트 완료: 주문 ID = {}", orderId);

        updateStorePolicy(paymentInfo.getUserNo(), quantity);
        log.info("store_settlements 정책 업데이트 완료: userNo = {}", paymentInfo.getUserNo());

        paymentInfo.setSbscrBgngDt(startDate);
        paymentInfo.setSbscrEndDt(endDate);
        paymentInfo.setSbscrStlmSttsCd("COMPLETED");
        paymentInfo.setStlmCmptnDt(LocalDateTime.now());

        log.info("결제 성공 처리 완료. 최종 반환 정보: {}", paymentInfo);
        return paymentInfo;
    }

    // 상점 정산 정책을 업데이트하는 보조 메서드
    @Transactional
    private void updateStorePolicy(String storeId, int currentPurchaseQuantity) {
        log.info("상점 정산 정책 업데이트 시작. storeId: {}, currentPurchaseQuantity: {}", storeId, currentPurchaseQuantity);

        // 1. 해당 상점의 총 구독 구매 횟수 조회 (COMPLETED 상태 기준)
        int totalSubscriptionCount = sellerPremiumMapper.getUserTotalSubscriptionCount(storeId);
        log.info("현재 상점의 총 구독 구매 횟수 (COMPLETED 기준): {}", totalSubscriptionCount);

        // 2. 새로운 정책 ID 결정 로직
        String newPolicyId;
        if (totalSubscriptionCount >= 48) { // 4년 이상 (48개월 = 48회 구매)
            newPolicyId = "plcy_6"; // 수수료 10%
        } else if (totalSubscriptionCount >= 36) { // 3년 이상 (36개월 = 36회 구매)
            newPolicyId = "plcy_5"; // 수수료 11%
        } else if (totalSubscriptionCount >= 24) { // 2년 이상 (24개월 = 24회 구매)
            newPolicyId = "plcy_4"; // 수수료 12%
        } else if (totalSubscriptionCount >= 12) { // 1년 이상 (12개월 = 12회 구매)
            newPolicyId = "plcy_3"; // 수수료 13%
        } else if (totalSubscriptionCount >= 1) { // 1회 이상 구매 (프리미엄 구독권 구매)
            newPolicyId = "plcy_2"; // 수수료 14%
        } else {
            newPolicyId = "plcy_1"; // 기본 18% (이 경우는 사실상 도달하지 않거나 이전에 설정되어야 함)
        }
        log.info("계산된 새로운 정책 ID: {}", newPolicyId);

        // 3. store_settlements 테이블의 plcy_id 업데이트 (항상 UPDATE)
        sellerPremiumMapper.updateStoreSettlementPolicy(storeId, newPolicyId);
        log.info("store_settlements 테이블 업데이트 완료. storeId: {}, newPolicyId: {}", storeId, newPolicyId);
    }
}