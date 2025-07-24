// ks55team02/admin/adminpage/storeadmin/settlementdetailbystore/controller/SettlementDetailByStoreController.java

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
import org.springframework.web.bind.annotation.RequestParam;
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
    public String storeadminSettlementController(@ModelAttribute SearchCriteria searchCriteria, Model model) {
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

    /**
     * ★★★ 이제 인터페이스에 선언된 `getSettlementHistoryByStoreId`를 호출합니다. ★★★
     */
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
        if (success) {
            return ResponseEntity.ok(Map.of("message", "선택된 정산 건들이 성공적으로 처리되었습니다."));
        } else {
            return new ResponseEntity<>(Map.of("message", "정산 처리 중 일부 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * ★★★ 이제 인터페이스에 선언된 `getStoreAccountDetailsByStoreId`를 호출합니다. ★★★
     */
    @GetMapping("/api/account-details/{storeId}")
    @ResponseBody
    public ResponseEntity<StoreAccountDTO> getAccountDetails(@PathVariable String storeId) {
        StoreAccountDTO accountDetails = storeSettlementService.getStoreAccountDetailsByStoreId(storeId);
        if (accountDetails == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(accountDetails);
    }

    /**
     * ★★★ Form 전송 방식으로 정산을 처리하는 최종 메소드입니다. ★★★
     */
    @PostMapping("/api/process-single")
    @ResponseBody
    public ResponseEntity<Map<String, String>> processSingleSettlement(@RequestBody Map<String, String> payload) {
        String storeClclnId = payload.get("storeClclnId");
        if (storeClclnId == null || storeClclnId.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "정산 ID가 제공되지 않았습니다."), HttpStatus.BAD_REQUEST);
        }
        
        try {
            boolean success = storeSettlementService.completeAndCreateNewPendingSettlement(storeClclnId);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "정산이 성공적으로 처리되었습니다."));
            } else {
                return new ResponseEntity<>(Map.of("message", "정산 처리에 실패했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "정산 처리 중 오류가 발생했습니다: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}