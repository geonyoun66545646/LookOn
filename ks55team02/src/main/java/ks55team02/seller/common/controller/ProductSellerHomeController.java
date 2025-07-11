package ks55team02.seller.common.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ks55team02.seller.common.domain.Store;
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
    	String storeId ="store_25";

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

         // 뷰로 전달할 데이터를 Model에 추가합니다.
         model.addAttribute("title", "상점 상세 정보");
         model.addAttribute("store", store); // 조회된 Store DTO 객체
         
         // 상점 로고 이미지 경로도 필요하다면 여기서 추가적으로 조회하여 모델에 담을 수 있습니다.
         // String storeLogoPath = storeMngService.getStoreLogoPathById(storeId);
         // model.addAttribute("storeLogoPath", storeLogoPath);

         // Thymeleaf 템플릿의 경로를 반환합니다.
         // 예: src/main/resources/templates/seller/store/storeDetailView.html
        return "seller/sellerMain"; // 템플릿 경로는 그대로 유지
    }
}