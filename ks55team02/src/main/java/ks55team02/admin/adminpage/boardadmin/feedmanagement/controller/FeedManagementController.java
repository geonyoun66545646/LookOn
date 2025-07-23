package ks55team02.admin.adminpage.boardadmin.feedmanagement.controller;

import ks55team02.admin.adminpage.boardadmin.feedmanagement.domain.AdminFeed;
import ks55team02.admin.adminpage.boardadmin.feedmanagement.service.FeedManagementService;
import ks55team02.admin.common.domain.Pagination;
import ks55team02.admin.login.domain.AdminInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/adminpage/boardadmin/feedmanagement")
@RequiredArgsConstructor
@Slf4j
public class FeedManagementController {

    private final FeedManagementService feedManagementService;

    /**
     * 피드 관리 페이지 최초 로딩 핸들러
     */
    @GetMapping("/feedList")
    public String feedList(@ModelAttribute AdminFeed searchCriteria, Model model) {
        
        // 첫 로딩 시 기본 필터 조건 설정
        if (searchCriteria.getFilterConditions() == null || searchCriteria.getFilterConditions().isEmpty()) {
            searchCriteria.setFilterConditions(Arrays.asList("normal"));
        }
        // 첫 로딩 시 기본 정렬 조건 설정
        if (searchCriteria.getSortOrder() == null || searchCriteria.getSortOrder().isEmpty()) {
            searchCriteria.setSortOrder("crtDtDesc");
        }

        // [수정] DTO의 편의 메소드를 사용하여 int 값을 가져와서 부모의 pageSize에 설정
        int intPageSize = searchCriteria.getPsizeAsInt();
        searchCriteria.setPageSize(intPageSize);
        
        int totalCount = feedManagementService.getFeedCount(searchCriteria);
        Pagination pagination = new Pagination(totalCount, searchCriteria);
        searchCriteria.setOffset(pagination.getLimitStart());
        
        List<AdminFeed> feedList = feedManagementService.getFeedList(searchCriteria);

        model.addAttribute("title", "피드 관리");
        model.addAttribute("feedList", feedList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pagination", pagination);
        model.addAttribute("searchCriteria", searchCriteria);

        return "admin/adminpage/boardadmin/feedmanagement/adminFeedList";
    }

    /**
     * 피드 목록 비동기 검색 핸들러 (Fragment 반환)
     */
    @GetMapping("/feedSearch")
    public String searchFeedList(@ModelAttribute AdminFeed searchCriteria, Model model) {
        
        // [수정] DTO의 편의 메소드를 사용하여 int 값을 가져와서 부모의 pageSize에 설정
        int intPageSize = searchCriteria.getPsizeAsInt();
        searchCriteria.setPageSize(intPageSize);

        int totalCount = feedManagementService.getFeedCount(searchCriteria);
        Pagination pagination = new Pagination(totalCount, searchCriteria);
        searchCriteria.setOffset(pagination.getLimitStart());
        
        List<AdminFeed> feedList = feedManagementService.getFeedList(searchCriteria);

        model.addAttribute("feedList", feedList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pagination", pagination);
        model.addAttribute("searchCriteria", searchCriteria);
        
        return "admin/adminpage/boardadmin/feedmanagement/adminFeedList :: feedListFragment"; 
    }

    /**
     * 피드 상태 일괄 변경 API (숨김/복구)
     */
    @PostMapping("/updateStatus")
    @ResponseBody
    public ResponseEntity<?> updateFeedsStatus(@RequestBody Map<String, Object> payload,
                                               @SessionAttribute(name="loginAdminInfo", required = false) AdminInfo loginAdmin) {
        
        if (loginAdmin == null) {
            return ResponseEntity.status(401).body(Map.of("result", "fail", "message", "관리자 로그인이 필요합니다."));
        }
        
        String adminUserNo = loginAdmin.getUserNo(); 
        
        List<String> feedSns = (List<String>) payload.get("feedSns");
        String action = (String) payload.get("action");

        try {
            if ("hide".equals(action)) {
                feedManagementService.hideFeeds(feedSns, adminUserNo);
            } else if ("restore".equals(action)) {
                feedManagementService.restoreFeeds(feedSns);
            } else {
                return ResponseEntity.badRequest().body(Map.of("result", "fail", "message", "알 수 없는 요청입니다."));
            }
            return ResponseEntity.ok(Map.of("result", "success", "message", "피드 상태가 성공적으로 변경되었습니다."));
        } catch (Exception e) {
            log.error("피드 상태 변경 중 서버 오류 발생", e);
            return ResponseEntity.status(500).body(Map.of("result", "fail", "message", "처리 중 오류가 발생했습니다."));
        }
    }
}