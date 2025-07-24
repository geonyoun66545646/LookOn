package ks55team02.seller.settlements.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import ks55team02.customer.login.domain.LoginUser;
import ks55team02.seller.settlements.domain.SalesSttsDTO;
import ks55team02.seller.settlements.domain.SettlementSearchCriteria;
import ks55team02.seller.settlements.service.SettlementService;

@Controller
@RequestMapping("/seller/settlement")
public class SettlementController {
	
	private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping("/settlementHistory")
    public String getMySettlementList(
            Model model,
            @ModelAttribute SettlementSearchCriteria criteria,
            @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser,
            @RequestParam(name = "filterConditions", required = false) List<String> testFilterConditions
    ) {
    	 System.out.println("전달받은 Criteria: " + criteria.toString());
    	    if (criteria.getFilterConditions() != null) {
    	        System.out.println("수신된 filterConditions:");
    	        criteria.getFilterConditions().forEach(condition -> 
    	            System.out.println("- " + condition));
    	    }
        // --- 디버그 로그 추가 시작 ---
        System.out.println("--- SettlementController: getMySettlementList 호출 ---");
        System.out.println("전달받은 Criteria: " + criteria.toString()); // SettlementSearchCriteria에 @ToString이 있다면 유용
        List<String> receivedFilterConditions = criteria.getFilterConditions();
        if (receivedFilterConditions != null && !receivedFilterConditions.isEmpty()) {
            System.out.println("수신된 filterConditions:");
            for (String condition : receivedFilterConditions) {
                System.out.println("- " + condition);
            }
            criteria.setFilterConditions(testFilterConditions);
        } else {
            System.out.println("수신된 filterConditions가 없거나 비어 있습니다.");
        }
        System.out.println("--- 디버그 로그 종료 ---");
        // --- 디버그 로그 추가 종료 ---

        String storeId = null;

        if (loginUser == null || loginUser.getUserNo() == null || loginUser.getUserNo().isEmpty()) {
            System.err.println("DEBUG: 세션에 로그인 정보(LoginUser)가 없거나 userNo가 비어 있습니다.");
            return "redirect:/main";
        }

        String userNo = loginUser.getUserNo();
        System.out.println("DEBUG: 세션에서 가져온 userNo: " + userNo);

        try {
            storeId = settlementService.getStoreIdByUserNo(userNo);
            System.out.println("DEBUG: userNo " + userNo + " 에 연결된 storeId: " + storeId);
        } catch (Exception e) {
            System.err.println("ERROR: userNo " + userNo + " 에 해당하는 storeId를 찾는 중 오류 발생: " + e.getMessage());
            return "redirect:/main?error=storeIdFetchFailed";
        }

        if (storeId == null || storeId.isEmpty()) {
            System.err.println("DEBUG: userNo " + userNo + " 에 연결된 storeId가 없습니다. (데이터베이스에 매핑되지 않음)");
            return "redirect:/main?error=noStoreFound";
        }

        criteria.setStoreId(storeId);

        if (criteria.getPageSize() == 0) {
            criteria.setPageSize(10);
        }

        // 여기서 criteria 객체가 filterConditions 값을 제대로 가지고 service로 전달되는지 확인
        Map<String, Object> data = settlementService.getMySettlementList(criteria);

        model.addAttribute("pagination", data.get("pagination"));
        model.addAttribute("searchCriteria", data.get("searchCriteria"));

        return "seller/fragments/settlementHistory";
    }
    
 // ★★★ 1. 이 메소드를 새로 추가해주세요. ★★★
    /**
     * 전체 판매 내역 조회 페이지를 보여주는 메소드
     */
    @GetMapping("/list")
    public String salesHistoryListPage(Model model) {
        
        // 참고: 현재는 모든 판매 내역을 가져오지만, 나중에 검색/페이지네이션을 추가할 수 있습니다.
        // 여기서는 예시로 store_id 'store_01'의 내역만 가져오겠습니다.
        // 실제로는 전체 내역을 가져오거나, 검색 기능을 구현해야 합니다.
        List<SalesSttsDTO> salesHistoryList = settlementService.getSalesHistoryByStoreId("store_01"); // 예시 ID
        
        model.addAttribute("title", "전체 판매 내역 조회");
        model.addAttribute("salesHistoryList", salesHistoryList);
        
        // 반환하는 경로는 새로 만들 HTML 파일의 위치와 일치해야 합니다.
        return "seller/fragments/salesStts";
    }
    
    /**
     * 특정 상점의 판매 내역을 조회하는 API 엔드포인트
     * @param storeId URL 경로에서 받아온 상점 ID
     * @return 판매 내역 목록을 담은 ResponseEntity
     */
    @GetMapping("/api/{storeId}")
    @ResponseBody
    public ResponseEntity<List<SalesSttsDTO>> getSalesHistory(@PathVariable String storeId) {
        List<SalesSttsDTO> salesHistory = settlementService.getSalesHistoryByStoreId(storeId);
        return ResponseEntity.ok(salesHistory);
    }
}
