package ks55team02.admin.adminpage.storeadmin.storemngadmin.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

import ks55team02.admin.adminpage.storeadmin.storemngadmin.service.StoreMngAdminService;
import ks55team02.admin.common.domain.Pagination;
import ks55team02.common.domain.store.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/adminpage/storeadmin")
@RequiredArgsConstructor
@Log4j2
public class StoreMngAdminController {

    private final StoreMngAdminService storeMngAdminService;

    @GetMapping("/storeMngAdmin")
    public String storeAdminManagementView(
            Model model,
            Store store
    ) {
        log.info("컨트롤러: storeAdminManagementView 호출 - 현재 페이지: {}, 페이지 크기: {}, 검색 키: {}, 검색 값: {}",
                 store.getCurrentPage(), store.getPageSize(), store.getSearchKey(), store.getSearchValue());
        log.info("디버그 확인: store.filterConditions = {}", store.getFilterConditions());

        int totalRecordCount = storeMngAdminService.getStoreCount(store);

        Pagination pagination = new Pagination(totalRecordCount, store);
        log.info("컨트롤러: pagination 계산 완료 - totalPageCount: {}, startPage: {}, endPage: {}, limitStart: {}",
                 pagination.getTotalPageCount(), pagination.getStartPage(), pagination.getEndPage(), pagination.getLimitStart());

        store.setOffset(pagination.getLimitStart());

        List<Store> storeList = storeMngAdminService.getStoreList(store, pagination.getLimitStart(), store.getPageSize());
        log.info("컨트롤러: storeList 조회 결과 개수: {}", storeList.size());

        model.addAttribute("title", "상점 관리");
        model.addAttribute("storeList", storeList);
        model.addAttribute("pagination", pagination);
        model.addAttribute("searchCriteria", store);

        return "admin/adminpage/storeadmin/storeMngAdminView";
    }

    @PostMapping("/updateStoreStatus")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStoreStatus(@RequestBody Map<String, Object> payload) {
        try {
            @SuppressWarnings("unchecked")
            List<String> storeIds = (List<String>) payload.get("storeIds");
            String newStatus = (String) payload.get("newStatus");

            if (storeIds == null || storeIds.isEmpty() || newStatus == null || newStatus.isEmpty()) {
                return new ResponseEntity<>(Map.of("success", false, "message", "필수 파라미터가 누락되었습니다."), HttpStatus.BAD_REQUEST);
            }

            storeMngAdminService.updateStoreStatus(storeIds, newStatus);

            return new ResponseEntity<>(Map.of("success", true, "message", "상점 상태가 성공적으로 변경되었습니다."), HttpStatus.OK);
        } catch (Exception e) {
            log.error("상점 상태 변경 중 오류 발생: {}", e.getMessage(), e);
            return new ResponseEntity<>(Map.of("success", false, "message", "상점 상태 변경 중 내부 서버 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
}