package ks55team02.seller.premiumcheckout.service.impl;

import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.seller.premiumcheckout.domain.SellerPremiumDTO;
import ks55team02.seller.premiumcheckout.domain.SellerPremiumPaymentDTO;
import ks55team02.seller.premiumcheckout.mapper.SellerPremiumMapper;
import ks55team02.seller.premiumcheckout.service.SellerPremiumService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class SellerPremiumServiceImpl implements SellerPremiumService { // 또는 SellerPremiumServiceImpl

	// ✅ final 키워드로 Mapper만 깔끔하게 주입받습니다.
    private final SellerPremiumMapper sellerPremiumMapper;

    // ❌ [삭제] 수동 트랜잭션 관리자는 더 이상 필요 없습니다.
    // private final PlatformTransactionManager transactionManager;
    
    // ❌ [삭제] ServiceImpl에는 RestTemplate과 ObjectMapper가 필요 없습니다. (필요하다면 유지)
    // private final RestTemplate restTemplate;
    // private final ObjectMapper objectMapper;
    
    // ❌ [삭제] Toss Secret Key는 결제 '승인' 시에만 필요하므로, 여기서는 일단 제외합니다.
    // @Value("${toss.secret-key}")
    // private String secretKey;

    // @PostConstruct
    // public void init() { ... }


    // --- 기존의 목록/상세 조회 메서드는 그대로 둡니다 ---
    @Override
    public int getSubscriptionPlanCount(SearchCriteria searchCriteria) {
        return sellerPremiumMapper.getSubscriptionPlanCount(searchCriteria);
    }
    @Override
    public List<SellerPremiumDTO> getSubscriptionPlanList(SearchCriteria searchCriteria) {
        return sellerPremiumMapper.getSubscriptionPlanList(searchCriteria);
    }
    @Override
    public SellerPremiumDTO getSubscriptionPlanById(String sbscrPlanId) {
        return sellerPremiumMapper.getSubscriptionPlanById(sbscrPlanId);
    }

    /**
     * ✅ [최종 수정] 주문 생성 메서드
     * Spring의 기본 @Transactional 기능을 사용하여 안정적으로 INSERT를 COMMIT합니다.
     */
    @Override
    @Transactional
    public Map<String, Object> createPremiumOrder(Map<String, Object> payload, String userNo, String userName) {
        
        String planId = (String) payload.get("planId");
        String planName = (String) payload.get("planName");
        int quantity = (int) payload.get("quantity");
        BigDecimal totalAmount = new BigDecimal(payload.get("amount").toString());
        String orderId = "prem_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);

        SellerPremiumPaymentDTO paymentOrder = new SellerPremiumPaymentDTO();
        paymentOrder.setSbscrStlmId(orderId);
        paymentOrder.setUserNo(userNo);
        paymentOrder.setSbscrPlanId(planId);
        paymentOrder.setSbscrPrchsNocs(quantity);
        paymentOrder.setSbscrTotStlmAmt(totalAmount);

        // 이 메서드가 성공적으로 끝나면 @Transactional이 자동으로 COMMIT 해줍니다.
        sellerPremiumMapper.insertPremiumOrder(paymentOrder);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("orderName", planName);
        response.put("customerName", userName);
        response.put("amount", totalAmount.intValue());

        return response;
    }

    /**
     * ✅ [최종 수정] 결제 성공 처리 메서드
     * 이 로직은 이미 완벽하므로 그대로 유지합니다.
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SellerPremiumPaymentDTO processPaymentSuccess(String orderId, String paymentKey, BigDecimal amount, String userNo) {
        log.info("결제 성공 처리 시작. 주문 ID: {}", orderId);

        SellerPremiumPaymentDTO paymentInfo = sellerPremiumMapper.findPaymentWithPlanNameByOrderId(orderId);
        if (paymentInfo == null) {
            throw new RuntimeException("주문 생성 기록을 찾을 수 없습니다: " + orderId);
        }
        
        int quantity = paymentInfo.getSbscrPrchsNocs();
        String planId = paymentInfo.getSbscrPlanId();
        
        int planTermDays = sellerPremiumMapper.findPlanTermById(planId);
        long totalSubscriptionDays = (long) planTermDays * quantity;
        
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(totalSubscriptionDays);

        sellerPremiumMapper.updatePaymentSuccess(orderId, paymentKey, startDate, endDate);

        paymentInfo.setSbscrBgngDt(startDate);
        paymentInfo.setSbscrEndDt(endDate);
        paymentInfo.setSbscrStlmSttsCd("COMPLETED");
        
        return paymentInfo;
    }


}
