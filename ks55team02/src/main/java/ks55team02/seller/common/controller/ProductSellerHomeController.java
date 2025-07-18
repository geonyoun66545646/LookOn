package ks55team02.seller.common.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.seller.common.domain.Store;
import ks55team02.seller.common.domain.TopSellingProduct;
import ks55team02.seller.common.service.impl.StoreMngService;
import ks55team02.seller.inquiry.service.SellerInquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Controller
@RequestMapping(value = "/seller")
@Log4j2
public class ProductSellerHomeController {

    private final StoreMngService storeMngService;
    private final SellerInquiryService sellerInquiryService;

    @GetMapping(value = {"", "/"})
    public String getStoreInfo(
            Model model,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        String userLoginId = null;
        String storeId = null;

        if (loginUser != null) {
            userLoginId = loginUser.getUserLgnId();
            log.info("판매자 홈 컨트롤러: 로그인 사용자 ID: {}", userLoginId);

            storeId = sellerInquiryService.getStoreIdByUserLgnId(userLoginId);
            log.info("판매자 홈 컨트롤러: 상점 ID: {}", storeId);
        } else {
            log.warn("세션에 로그인 정보가 없습니다.");
            redirectAttributes.addFlashAttribute("errorMessage", "로그인 정보가 없습니다. 다시 로그인해주세요.");
            redirectAttributes.addFlashAttribute("showAlert", true);
            return "redirect:/seller/login";
        }

        if (storeId == null) {
            log.warn("상점 ID 없음. 로그인 사용자 ID: {}", userLoginId);
            redirectAttributes.addFlashAttribute("errorMessage", "상점 정보가 확인되지 않습니다. 상점 신청을 완료해주세요.");
            redirectAttributes.addFlashAttribute("showAlert", true);

            String referer = request.getHeader("Referer");
            String redirectUrl = (referer != null && !referer.contains("/seller")) ? referer : "/seller/login";

            return "redirect:" + redirectUrl;
        }

        Store store = storeMngService.getStoreInfoById(storeId);
        if (store == null) {
            log.warn("상점 ID {}에 해당하는 정보 없음.", storeId);
            redirectAttributes.addFlashAttribute("errorMessage", "요청하신 상점 정보를 찾을 수 없습니다.");
            redirectAttributes.addFlashAttribute("showAlert", true);

            String referer = request.getHeader("Referer");
            String redirectUrl = (referer != null && !referer.contains("/seller")) ? referer : "/seller/login";

            return "redirect:" + redirectUrl;
        }

        Map<String, Object> subscription = storeMngService.getStoreSubscriptionByStoreId(storeId);
        boolean hasValidSubscription = subscription != null
                && !subscription.isEmpty()
                && subscription.get("earliest_sbscr_bgng_dt") != null;

        if (!hasValidSubscription) {
            model.addAttribute("hasSubscription", false);
            model.addAttribute("sbscrPrchsNocs", 0);
        } else {
            model.addAttribute("hasSubscription", true);
            model.addAttribute("earliestSubscriptionStartDate", subscription.get("earliest_sbscr_bgng_dt"));
            model.addAttribute("latestSubscriptionEndDate", subscription.get("latest_sbscr_end_dt"));
            
            Object sbscrPrchsNocsValue = subscription.get("sbscr_prchs_nocs");
            model.addAttribute("sbscrPrchsNocs", sbscrPrchsNocsValue != null ? sbscrPrchsNocsValue : 0);
        }

        Long storeSettle = storeMngService.getTotalSettleById(storeId);
        Long storeOrdCnt = storeMngService.getTotalOrderById(storeId);
        Long storeActGds = storeMngService.getActGdsById(storeId);
        List<TopSellingProduct> topSellingProducts = storeMngService.getTopSellingProductsByStoreId(storeId);

        log.info("--- [Top Selling Products for store {}] ---", storeId);
        if (topSellingProducts != null && !topSellingProducts.isEmpty()) {
            for (TopSellingProduct product : topSellingProducts) {
                if (product.getRanking() != null) {
                    String gdsNm = product.getRanking().getGdsNm();
                    Long totalSold = product.getTotalSoldQuantity();
                    LocalDateTime regDt = product.getRanking().getRegDt();

                    log.info("상품명: {}, 총 판매 수량: {}, 등록일: {}", gdsNm, totalSold, regDt);
                } else {
                    log.warn("판매 랭킹 정보가 없는 상품 항목 있음");
                }
            }
        } else {
            log.info("판매 랭킹 상품 없음");
        }

        model.addAttribute("title", "판매자 대시보드");
        model.addAttribute("store", store);
        model.addAttribute("storeSettle", storeSettle);
        model.addAttribute("storeOrdCnt", storeOrdCnt);
        model.addAttribute("storeActGds", storeActGds);
        model.addAttribute("topSellingProduct", topSellingProducts);

        return "seller/sellerMain";
    }
}