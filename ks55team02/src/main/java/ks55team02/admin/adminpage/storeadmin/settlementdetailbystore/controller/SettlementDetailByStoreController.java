package ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreAccountDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementListViewDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.service.StoreSettlementService;
import ks55team02.admin.common.domain.Pagination;
import ks55team02.admin.common.domain.SearchCriteria;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/adminpage/storeadmin")
@RequiredArgsConstructor
public class SettlementDetailByStoreController {

    private final StoreSettlementService storeSettlementService;

    @GetMapping("/settlementDetailByStore")
    public String storeadminSettlementController(
            @ModelAttribute SearchCriteria searchCriteria,
            Model model
    ) {
        // ⭐ 이전에 추가했던 기본 '판매정산대기' 필터 설정 코드를 제거합니다.
        // 이 부분을 주석 처리하거나 삭제하세요.
        // if (searchCriteria.getSearchKey() == null || searchCriteria.getSearchKey().isEmpty() ||
        //     !"clclnSttsCd".equals(searchCriteria.getSearchKey()) ||
        //     searchCriteria.getSearchValue() == null || searchCriteria.getSearchValue().isEmpty()) {
        //     
        //     searchCriteria.setSearchKey("clclnSttsCd");
        //     searchCriteria.setSearchValue("판매정산대기");
        // }

        if (searchCriteria.getCurrentPage() <= 0) {
            searchCriteria.setCurrentPage(1);
        }

        int totalRecordCount = storeSettlementService.getTotalSettlementCount(searchCriteria);
        Pagination pagination = new Pagination(totalRecordCount, searchCriteria);
        List<StoreSettlementListViewDTO> settlementList = storeSettlementService.getAllStoreSettlementsForList(searchCriteria);

        model.addAttribute("title", "스토어별 정산 내역");
        model.addAttribute("settlementList", settlementList);
        model.addAttribute("pagination", pagination);
        model.addAttribute("searchCriteria", searchCriteria);

        return "admin/adminpage/storeadmin/settlementDetailByStore";
    }

    // --- API 요청을 처리하는 메서드들 (기존 내용 유지) ---
    @GetMapping("/api/history/{storeId}")
    @ResponseBody
    public ResponseEntity<List<StoreSettlementDTO>> getStoreSettlementHistory(@PathVariable String storeId) {
        List<StoreSettlementDTO> historyList = storeSettlementService.getSettlementHistoryByStoreId(storeId);
        return ResponseEntity.ok(historyList);
    }

    @PostMapping("/api/process-batch")
    @ResponseBody
    public ResponseEntity<Map<String, String>> processBatchSettlement(@RequestBody Map<String, List<String>> payload) {
        List<String> storeClclnIds = payload.get("storeClclnIds");

        if (storeClclnIds == null || storeClclnIds.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "처리할 정산 ID가 없습니다."), HttpStatus.BAD_REQUEST);
        }

        boolean success = storeSettlementService.completeBatchSettlements(storeClclnIds);

        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("message", "선택된 정산 건들이 성공적으로 처리되었습니다.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "정산 처리 중 일부 오류가 발생했습니다.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/process-single")
    @ResponseBody
    public ResponseEntity<Map<String, String>> processSingleSettlement(@RequestBody Map<String, String> payload) {
        String storeClclnId = payload.get("storeClclnId");

        if (storeClclnId == null || storeClclnId.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "처리할 정산 ID가 없습니다."), HttpStatus.BAD_REQUEST);
        }

        boolean success = storeSettlementService.completeSettlement(storeClclnId);

        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("message", "정산이 성공적으로 처리되었습니다.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "정산 처리 중 오류가 발생했습니다.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/account-details/{storeId}")
    @ResponseBody
    public ResponseEntity<StoreAccountDTO> getAccountDetails(@PathVariable String storeId) {
        StoreAccountDTO accountDetails = storeSettlementService.getStoreAccountDetailsByStoreId(storeId);

        if (accountDetails == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(accountDetails);
    }
}