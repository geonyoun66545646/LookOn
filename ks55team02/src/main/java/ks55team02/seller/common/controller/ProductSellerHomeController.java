package ks55team02.seller.common.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ks55team02.seller.common.domain.Store;
import ks55team02.seller.common.domain.TopSellingProduct;
import ks55team02.seller.common.service.impl.StoreMngService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Controller
@RequestMapping(value="/seller") // URL을 /seller로 변경
@Log4j2
public class ProductSellerHomeController {

	private final StoreMngService storeMngService;
	
    @GetMapping(value={"", "/"})
	public String getStoreInfo(/* @RequestParam("storeId") String storeId, */ Model model) {
    	String storeId ="store_1";

         // 서비스 계층을 통해 storeId로 상점 정보를 조회합니다.
         Store store = storeMngService.getStoreInfoById(storeId);

         // 조회된 상점 정보가 없을 경우 처리 (예: 404 페이지 또는 메시지)
         if (store == null) {
             log.warn("상점 ID {}에 해당하는 상점 정보를 찾을 수 없습니다.", storeId);
             // 에러 페이지로 리다이렉트하거나, 모델에 에러 메시지를 추가할 수 있습니다.
             model.addAttribute("errorMessage", "요청하신 상점 정보를 찾을 수 없습니다.");
             return "error/404"; // 예시: 에러 페이지 경로
         }
         
         // 2. 상점의 구독 기간 통합 정보 조회 (시작 처음과 끝)
         Map<String, Object> subscription = storeMngService.getStoreSubscriptionByStoreId(storeId);
         if (subscription == null || subscription.isEmpty() || subscription.get("earliest_sbscr_bgng_dt") == null) {
             log.info("상점 ID {}에 대한 구독 기록이 없거나 유효한 기간 정보가 없습니다.", storeId);
             model.addAttribute("hasSubscription", false);
             model.addAttribute("sbscrPrchsNocs", 0); 
         } else {
             model.addAttribute("hasSubscription", true);
             model.addAttribute("earliestSubscriptionStartDate", subscription.get("earliest_sbscr_bgng_dt"));
             model.addAttribute("latestSubscriptionEndDate", subscription.get("latest_sbscr_end_dt"));
             model.addAttribute("sbscrPrchsNocs", subscription.get("sbscr_prchs_nocs"));
         }

         // 3. 상점의 총 판매 금액 조회
         Long storeSettle = storeMngService.getTotalSettleById(storeId);
         
         // 4. 상점의 총 상품 판매 개수 조회
         Long storeOrdCnt = storeMngService.getTotalOrderById(storeId);
         
         // 5. 상점의 총 활성화된 상품 개수
         Long storeActGds = storeMngService.getActGdsById(storeId);
         
         // 6. 상점의 탑5 판매 랭킹
         List<TopSellingProduct> topSellingProducts = storeMngService.getTopSellingProductsByStoreId(storeId);
         log.info("--- [DEBUG] Top Selling Products for store {} ---", storeId);
         if (topSellingProducts != null && !topSellingProducts.isEmpty()) {
             for (TopSellingProduct product : topSellingProducts) {
                 // product.getRanking()이 null이 아닌지 확인 (상품명이 나오므로 null은 아닐 것입니다.)
                 String gdsNm = (product.getRanking() != null) ? product.getRanking().getGdsNm() : "N/A";
                 Long totalSold = product.getTotalSoldQuantity(); // 이 값이 무엇인지 확인
                 LocalDateTime regDt = (product.getRanking() != null) ? product.getRanking().getRegDt() : null; // 이 값이 무엇인지 확인

                 log.info("  상품명: {}, 총 판매 수량: {}, 등록일: {}", gdsNm, totalSold, regDt);
             }
         } else {
             log.info("  상점 {}에 대한 판매 랭킹 상품이 없습니다.", storeId);
         }
         log.info("--- [DEBUG] End Top Selling Products ---");
         
         
         // 뷰로 전달할 데이터를 Model에 추가합니다.
         model.addAttribute("title", "상점 상세 정보");
         model.addAttribute("store", store); // 조회된 Store DTO 객체
         model.addAttribute("storeSettle", storeSettle); // 조회된 Store DTO 객체
         model.addAttribute("storeOrdCnt", storeOrdCnt); // 조회된 Store DTO 객체
         model.addAttribute("storeActGds", storeActGds); // 조회된 Store DTO 객체
         model.addAttribute("topSellingProduct", topSellingProducts); // 조회된 Store DTO 객체
         
         // 상점 로고 이미지 경로도 필요하다면 여기서 추가적으로 조회하여 모델에 담을 수 있습니다.
         // String storeLogoPath = storeMngService.getStoreLogoPathById(storeId);
         // model.addAttribute("storeLogoPath", storeLogoPath);

         // Thymeleaf 템플릿의 경로를 반환합니다.
         // 예: src/main/resources/templates/seller/store/storeDetailView.html
        return "seller/sellerMain"; // 템플릿 경로는 그대로 유지
    }
}