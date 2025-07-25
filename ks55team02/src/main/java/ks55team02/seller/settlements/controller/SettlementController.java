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
import org.springframework.web.bind.annotation.RequestParam; // @RequestParam 임포트 유지
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import ks55team02.customer.login.domain.LoginUser;
import ks55team02.seller.settlements.domain.SalesSttsDTO;
import ks55team02.seller.settlements.domain.SettlementSearchCriteria;
import ks55team02.seller.settlements.service.SettlementService;
import ks55team02.admin.common.domain.Pagination; // Pagination 클래스 임포트
import ks55team02.admin.common.domain.SearchCriteria; // SearchCriteria 클래스 임포트

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
            @ModelAttribute SearchCriteria criteria,
            @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser
    ) {
        if (loginUser == null || loginUser.getUserNo() == null) {
            return "redirect:/main";
        }
        String userNo = loginUser.getUserNo();
        String storeId = settlementService.getStoreIdByUserNo(userNo);
        if (storeId == null) {
            return "redirect:/main?error=noStoreFound";
        }
        criteria.setStoreId(storeId);

        // 1. 서비스로부터 원본 데이터를 받습니다.
        // 이 data 맵은 "settlementList" (List<SettlementDTO>)와 "pagination" (Pagination 객체)를 포함합니다.
        Map<String, Object> data = settlementService.getMySettlementList(criteria);

        // ⭐⭐⭐ 핵심 수정 부분: HashMap 변환 과정을 제거하고 원본 Pagination 객체를 직접 모델에 추가합니다. ⭐⭐⭐
        // 이렇게 하면 Thymeleaf가 Pagination 클래스의 필드(existPrevBlock, currentPage 등)에 직접 접근할 수 있습니다.
        model.addAttribute("settlementList", data.get("settlementList"));
        model.addAttribute("pagination", data.get("pagination")); // 여기만 수정!

        model.addAttribute("searchCriteria", criteria);
        model.addAttribute("title", "나의 정산 내역");

        return "seller/settlement/settlementHistory";
    }

    /**
     * 전체 판매 내역 조회 페이지를 보여주는 메소드
     */
    @GetMapping("/list")
    public String salesHistoryListPage(
            Model model,
            @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser,
            @RequestParam(value = "page", defaultValue = "1") int currentPage,
            @RequestParam(value = "size", defaultValue = "10") int pageSize,
            @RequestParam(value = "searchKey", required = false) String searchKey, // ⭐⭐ searchKey 추가 ⭐⭐
            @RequestParam(value = "searchValue", required = false) String searchValue // ⭐⭐ searchValue 추가 ⭐⭐
    ) {
        if (loginUser == null || loginUser.getUserNo() == null) {
            return "redirect:/main";
        }

        String userNo = loginUser.getUserNo();
        String storeId = settlementService.getStoreIdByUserNo(userNo);

        if (storeId == null) {
            return "redirect:/main?error=noStoreFound";
        }

        // 1. SettlementSearchCriteria 객체 준비
        // 이 객체에 모든 검색 조건을 담아서 서비스/매퍼로 전달합니다.
        SettlementSearchCriteria salesCriteria = new SettlementSearchCriteria();
        salesCriteria.setStoreId(storeId);
        salesCriteria.setCurrentPage(currentPage);
        salesCriteria.setPageSize(pageSize);
        salesCriteria.setSearchKey(searchKey);     // ⭐⭐ 컨트롤러에서 받은 searchKey 설정 ⭐⭐
        salesCriteria.setSearchValue(searchValue); // ⭐⭐ 컨트롤러에서 받은 searchValue 설정 ⭐⭐

        // 2. 전체 판매 내역 개수 조회
        // searchKey와 searchValue가 포함된 salesCriteria를 전달합니다.
        int totalRecordCount = settlementService.getSalesHistoryCountByStoreId(salesCriteria);

        // 3. Pagination 객체 생성 및 계산
        // Pagination DTO를 수정하지 않기 위해 SearchCriteria 익명 클래스를 그대로 사용합니다.
        // 이 익명 클래스는 Pagination 계산에 필요한 currentPage와 pageSize만 제공합니다.
        SearchCriteria paginationSearchCriteria = new SearchCriteria() {
            @Override
            public int getCurrentPage() {
                return currentPage; // 컨트롤러의 currentPage 파라미터 사용
            }

            @Override
            public int getPageSize() {
                return pageSize; // 컨트롤러의 pageSize 파라미터 사용
            }
            // SearchCriteria 클래스의 나머지 getter들은 기본값(null 또는 0)을 반환하거나,
            // 이 익명 클래스가 사용되는 Pagination 생성자 내부에서만 호출되므로 문제가 없습니다.
            @Override public int getOffset() { return 0; }
            @Override public String getSortOrder() { return null; }
            @Override public String getSortKey() { return null; }
            @Override public java.time.LocalDate getStartDate() { return null; }
            @Override public java.time.LocalDate getEndDate() { return null; }
            @Override public String getSearchKey() { return null; } // 여기서 실제 searchKey를 사용하지 않으므로 null 반환
            @Override public String getSearchValue() { return null; } // 여기서 실제 searchValue를 사용하지 않으므로 null 반환
            @Override public java.util.List<String> getFilterConditions() { return null; }
            @Override public java.util.List<Integer> getLevels() { return null; }
            @Override public String getStoreId() { return null; }
            @Override public String getSelUserNo() { return null; }
        };

        // 익명 클래스로 생성된 searchCriteria를 Pagination 생성자에 전달
        Pagination pagination = new Pagination(totalRecordCount, paginationSearchCriteria);

        // 4. 실제 페이지에 해당하는 판매 내역 리스트 조회
        salesCriteria.setOffset(pagination.getLimitStart()); // Pagination DTO에서 계산된 limitStart (OFFSET) 사용
        // `Pagination`의 `recordSize`는 `pageSize`와 동일하므로, `setLimit` 메서드를 `salesCriteria`에 추가하고 사용합니다.
        // SettlementSearchCriteria에 `int limit;` 필드를 추가하고 `setLimit(int limit)` 메서드를 정의해야 합니다.
        salesCriteria.setLimit(pagination.getRecordSize()); // ⭐⭐ SettlementSearchCriteria에 limit 필드 및 setter 필요 ⭐⭐

        List<SalesSttsDTO> salesStts = settlementService.getSalesHistoryByStoreIdAndPagination(salesCriteria);

        // 5. Model에 데이터 추가
        model.addAttribute("title", "나의 판매 현황");
        model.addAttribute("salesStts", salesStts);
        model.addAttribute("pagination", pagination);
        model.addAttribute("searchCriteria", salesCriteria); // 뷰로 `searchKey`, `searchValue`가 포함된 `salesCriteria` 객체 전달

        // 6. HTML 파일 경로 반환
        return "seller/fragments/salesStts";
    }

    /**
     * 특정 상점의 판매 내역을 조회하는 API 엔드포인트
     */
    @GetMapping("/api/{storeId}")
    @ResponseBody
    public ResponseEntity<List<SalesSttsDTO>> getSalesHistory(@PathVariable String storeId) {
        List<SalesSttsDTO> salesHistory = settlementService.getSalesHistoryByStoreId(storeId);
        return ResponseEntity.ok(salesHistory);
    }
}