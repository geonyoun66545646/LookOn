package ks55team02.seller.premiumcheckout.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import ks55team02.admin.common.domain.Pagination;
import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.seller.premiumcheckout.domain.SellerPremiumDTO;
import ks55team02.seller.premiumcheckout.domain.SellerPremiumPaymentDTO;
import ks55team02.seller.premiumcheckout.mapper.SellerPremiumMapper;
import ks55team02.seller.premiumcheckout.service.SellerPremiumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/seller/premium") // ⭐ 이 부분을 /seller/premium으로 수정합니다! ⭐
@RequiredArgsConstructor
@Slf4j
public class SellerPremiumController {

    private final SellerPremiumService sellerPremiumService;
    private final SellerPremiumMapper sellerPremiumMapper;
    
    @Value("${toss.client-key}") // application.properties에서 client-key 주입
    private String tossClientKey;

    @GetMapping("/checkout")
    public String showCheckoutPage(@RequestParam("planId") String planId,
                                   @RequestParam("planName") String planName,
                                   @RequestParam("amount") BigDecimal amount,
                                   Model model,
                                   HttpSession session) {
        
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            log.warn("로그인되지 않은 사용자가 결제 페이지 접근 시도.");
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }

        // View(HTML)로 필요한 모든 데이터를 전달
        model.addAttribute("tossClientKey", tossClientKey);
        model.addAttribute("planId", planId);
        model.addAttribute("planName", planName);
        model.addAttribute("amount", amount);
        model.addAttribute("userNo", loginUser.getUserNo()); // 실제 사용자 번호 필드명으로 변경
        model.addAttribute("userName", loginUser.getUserNcnm()); // 실제 사용자 이름 필드명으로 변경

        // `getPlanById` 같은 불필요한 DB 조회 및 JSON 변환 로직 제거
        return "seller/fragments/sellerPremiumCheckout";
    }
    
    @PostMapping("/api/create-order")
    @ResponseBody
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> payload, HttpSession session) {
        // ✅ API 호출 시작 로그
        log.info(">>>>>> /api/create-order API 호출 시작. payload: {}", payload);
        
        try {
            LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
            if (loginUser == null) {
                // 로그인 정보가 없을 때 명시적인 에러 반환
                log.error(">>>>>> 로그인 정보가 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
            }

            Map<String, Object> response = sellerPremiumService.createPremiumOrder(payload, loginUser.getUserNo(), loginUser.getUserNcnm());
            
            // ✅ 성공 로그
            log.info(">>>>>> 주문 생성 성공. 응답: {}", response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // ✅ 예기치 못한 모든 에러를 잡아서 로그를 남기고, JSON으로 에러 응답
            log.error(">>>>>> /api/create-order API 처리 중 심각한 오류 발생!", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "주문 생성 중 서버 오류가 발생했습니다."));
        }
    }
   

    // customerPremiumSubscriptionView() 메서드는 이전에 삭제되었거나 주석 처리되었다고 가정합니다.

    @GetMapping(value= {"/checkoutHistory"}) // 이 메서드는 이제 /seller/premium/checkoutHistory 에 매핑됩니다.
    public String premiumCheckoutHistoryView() {
        log.info("컨트롤러: premiumCheckoutHistoryView 호출 - 결제 내역 화면 로드");
        return "seller/fragments/sellerPremiumCheckoutHistory";
    }

    /**
     * 구독 플랜 목록 페이지를 조회합니다. (메인 목록 기능)
     * @param model Model 객체
     * @param searchCriteria 검색 및 페이징 조건
     * @param session HttpSession 객체 (로그인 사용자 정보 조회용)
     * @return 구독 플랜 목록 뷰 경로
     */
    @GetMapping("/sellerPremiumListView") // 이 메서드는 이제 /seller/premium/sellerPremiumListView 에 매핑됩니다.
    public String getSubscriptionPlanList(
            Model model,
            SearchCriteria searchCriteria,
            HttpSession session
    ) {
        log.info("컨트롤러: getSubscriptionPlanList 호출");
        log.info("  현재 페이지: {}", searchCriteria.getCurrentPage());
        log.info("  페이지 크기: {}", searchCriteria.getPageSize());
        log.info("  정렬 순서: {}", searchCriteria.getSortOrder());
        log.info("  시작일: {}", searchCriteria.getStartDate());
        log.info("  종료일: {}", searchCriteria.getEndDate());
        log.info("  검색 키: {}", searchCriteria.getSearchKey());
        log.info("  검색 값: {}", searchCriteria.getSearchValue());
        log.info("  필터 조건: {}", searchCriteria.getFilterConditions());

        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        String userLoginId = null;
        String storeId = null;

        if (loginUser != null) {
            userLoginId = loginUser.getUserLgnId();
            log.info("세션에서 가져온 로그인 사용자 ID: {}", userLoginId);
            storeId = "seller_store_001"; // 임시 상점 ID, 실제 로직으로 변경 필요
            log.info("로그인 사용자 ID({})에 해당하는 상점 ID: {}", userLoginId, storeId);
        } else {
            log.warn("세션에 로그인 정보가 없습니다.");
        }
        
        if (searchCriteria.getFilterConditions() != null && !searchCriteria.getFilterConditions().isEmpty()) {
            String firstFilterCondition = searchCriteria.getFilterConditions().get(0);
            if (firstFilterCondition != null && !firstFilterCondition.isEmpty()) {
                searchCriteria.setFilterConditions(Arrays.asList(firstFilterCondition.split(",")));
            } else {
                searchCriteria.setFilterConditions(null);
            }
        } else {
             searchCriteria.setFilterConditions(null);
        }

        int totalRecordCount = sellerPremiumService.getSubscriptionPlanCount(searchCriteria);
        log.info("컨트롤러: 총 구독 플랜 개수: {}", totalRecordCount);

        Pagination pagination = new Pagination(totalRecordCount, searchCriteria);
        log.info("컨트롤러: pagination 계산 완료 - totalPageCount: {}, startPage: {}, endPage: {}, limitStart: {}",
                 pagination.getTotalPageCount(), pagination.getStartPage(), pagination.getEndPage(), pagination.getLimitStart());

        searchCriteria.setOffset(pagination.getLimitStart());

        List<SellerPremiumDTO> subscriptionPlanList = sellerPremiumService.getSubscriptionPlanList(searchCriteria); // SellerPremiumDTO 대신 SubscriptionPlan 사용
        log.info("컨트롤러: 구독 플랜 목록 조회 결과 개수: {}", subscriptionPlanList.size());

        model.addAttribute("title", "구독 플랜 목록");
        model.addAttribute("subscriptionPlanList", subscriptionPlanList);
        model.addAttribute("pagination", pagination);
        model.addAttribute("searchCriteria", searchCriteria);

        return "seller/fragments/sellerPremiumListView";
    }

    /**
     * 특정 구독 플랜의 상세 정보를 조회합니다.
     * @param sbscrPlanId 조회할 구독 플랜 ID
     * @param model Model 객체
     * @return 구독 플랜 상세 뷰 경로
     */
    @GetMapping("/sellerPremiumDetail") // 이 메서드는 이제 /seller/premium/sellerPremiumDetail 에 매핑됩니다.
    public String getSubscriptionPlanDetail(@RequestParam("sbscrPlanId") String sbscrPlanId, Model model) {
        log.info("컨트롤러: getSubscriptionPlanDetail 호출 - 구독 플랜 ID: {}", sbscrPlanId);

        SellerPremiumDTO subscriptionPlan = sellerPremiumService.getSubscriptionPlanById(sbscrPlanId); // SellerPremiumDTO 대신 SubscriptionPlan 사용

        if (subscriptionPlan != null) {
            model.addAttribute("title", "구독 플랜 상세");
            model.addAttribute("subscriptionPlan", subscriptionPlan);
            log.info("컨트롤러: model에 subscriptionPlan 추가 완료");
            return "seller/fragments/sellerPremiumDetail";
        } else {
            log.warn("해당 구독 플랜 ID로 조회된 데이터가 없습니다: {}", sbscrPlanId);
            model.addAttribute("errorMessage", "구독 플랜 정보를 찾을 수 없습니다.");
            return "error/dataNotFound";
        }
    }
    
    @GetMapping("/success")
    public String paymentSuccess(
            @RequestParam String orderId,
            @RequestParam String paymentKey,
            @RequestParam BigDecimal amount, // int 대신 BigDecimal로 받습니다.
            HttpSession session,
            Model model) {

        log.info("결제 성공 콜백 수신 - orderId: {}", orderId);

        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login"; // 비로그인 시 로그인 페이지로
        }

        // 1. 서비스 호출: DB 업데이트 및 최종 정보 조회를 한 번에 처리
        SellerPremiumPaymentDTO paymentDetails = sellerPremiumService.processPaymentSuccess(orderId, paymentKey, amount, loginUser.getUserNo());

        // 2. [핵심] 서비스로부터 받은 DTO의 값들을 View로 전달하기 위해 Model에 추가합니다.
        if (paymentDetails != null) {
            model.addAttribute("orderId", paymentDetails.getSbscrStlmId());
            model.addAttribute("amount", paymentDetails.getSbscrTotStlmAmt());
            model.addAttribute("paymentMethod", paymentDetails.getSbscrStlmMthdCd());
            model.addAttribute("startDate", paymentDetails.getSbscrBgngDt());
            model.addAttribute("endDate", paymentDetails.getSbscrEndDt());
            model.addAttribute("planName", paymentDetails.getSbscrPlanNm()); // 플랜 이름도 추가
        } else {
            // 혹시 모를 예외 상황 처리
            model.addAttribute("orderId", orderId);
            model.addAttribute("amount", amount);
            model.addAttribute("errorMessage", "결제 정보를 조회하는 데 실패했습니다.");
        }

        model.addAttribute("title", "결제 성공");
        return "seller/fragments/PremiumCheckoutSuccess"; // 정확한 HTML 파일 경로
    }

    @GetMapping("/fail")
    public String paymentFail(
            @RequestParam String orderId,
            @RequestParam String message,
            @RequestParam String code,
            Model model) {
        log.warn("결제 실패 - orderId: {}, code: {}, message: {}", orderId, code, message);
        model.addAttribute("title", "결제 실패");
        model.addAttribute("message", message);
        model.addAttribute("code", code);
        return "seller/fragments/premiumCheckoutFail"; // 실패 페이지 뷰 경로
    }
}