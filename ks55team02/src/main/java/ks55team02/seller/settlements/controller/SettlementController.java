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
            @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser
    ) {
        // --- 1. 로그인 확인 및 상점 ID 조회 (기존 로직과 동일) ---
        if (loginUser == null || loginUser.getUserNo() == null) {
            return "redirect:/main"; // 로그인 안 했으면 메인으로
        }
        String userNo = loginUser.getUserNo();
        String storeId = settlementService.getStoreIdByUserNo(userNo);
        if (storeId == null) {
            return "redirect:/main?error=noStoreFound"; // 상점이 없으면 메인으로
        }
        criteria.setStoreId(storeId);

        // --- 2. 서비스 호출하여 데이터 가져오기 ---
        // 서비스가 settlementList와 pagination을 모두 담은 Map을 반환한다고 가정합니다.
        Map<String, Object> data = settlementService.getMySettlementList(criteria);

        // --- 3. Model에 데이터를 담아 HTML로 전달 ---
        // HTML이 기다리는 이름(${settlementList})과 똑같은 이름으로 데이터를 추가합니다.
        model.addAttribute("settlementList", data.get("settlementList"));
        model.addAttribute("pagination", data.get("pagination"));
        model.addAttribute("searchCriteria", criteria); // 검색 조건 유지를 위해 추가
        model.addAttribute("title", "나의 정산 내역"); // 페이지 제목 추가

        // --- 4. 보여줄 HTML 파일의 경로를 정확하게 지정 ---
        return "seller/settlement/settlementHistory"; // 예시 경로입니다. 실제 파일 위치에 맞게 수정하세요.
    }
    
    /**
     * 전체 판매 내역 조회 페이지를 보여주는 메소드
     */
    @GetMapping("/list")
    public String salesHistoryListPage(
            Model model,
            // 1. 세션에서 현재 로그인한 사용자 정보를 가져옵니다.
            @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser
    ) {
        
        // 2. 로그인 상태를 확인하고, 비로그인 시 메인 페이지로 보냅니다.
        if (loginUser == null || loginUser.getUserNo() == null) {
            // 로그인 정보가 없으면 판매 현황을 볼 수 없으므로 리다이렉트
            return "redirect:/main";
        }

        // 3. 세션에서 가져온 userNo로 해당 판매자의 store_id를 조회합니다.
        String userNo = loginUser.getUserNo();
        String storeId = settlementService.getStoreIdByUserNo(userNo);

        // 4. store_id가 없는 판매자일 경우를 대비한 방어 코드입니다.
        if (storeId == null) {
            // 상점이 없는 판매자는 판매 현황을 볼 수 없으므로 리다이렉트
            return "redirect:/main?error=noStoreFound";
        }
        
        List<SalesSttsDTO> salesHistoryList = settlementService.getSalesHistoryByStoreId(storeId);
        
        model.addAttribute("title", "나의 판매 현황");
        model.addAttribute("salesHistoryList", salesHistoryList);
        
        // 6. 보여줄 HTML 파일의 정확한 경로를 반환합니다.
        return "seller/settlement/settlementHistory"; // 실제 파일 위치에 맞게 수정 필요
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
