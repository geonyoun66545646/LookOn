package ks55team02.admin.adminpage.boardadmin.feedmanagement.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import ks55team02.admin.adminpage.boardadmin.feedmanagement.domain.AdminFeed;
import ks55team02.admin.adminpage.boardadmin.feedmanagement.service.FeedManagementService;
import ks55team02.admin.common.domain.Pagination;
import ks55team02.customer.login.domain.LoginUser; // [수정] 올바른 세션 DTO 임포트
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/adminpage/boardadmin/feedmanagement")
@RequiredArgsConstructor
@Slf4j
public class FeedManagementController {

    private final FeedManagementService feedManagementService;

    @GetMapping("/feedList")
    public String feedList(@ModelAttribute AdminFeed searchCriteria, Model model) {
        if (searchCriteria.getFilterConditions() == null || searchCriteria.getFilterConditions().isEmpty()) {
            searchCriteria.setFilterConditions(Arrays.asList("normal"));
        }
        if (searchCriteria.getSortOrder() == null || searchCriteria.getSortOrder().isEmpty()) {
            searchCriteria.setSortOrder("crtDtDesc");
        }
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

    @GetMapping("/feedSearch")
    public String searchFeedList(@ModelAttribute AdminFeed searchCriteria, Model model) {
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

    @PostMapping("/updateStatus")
    @ResponseBody
    public ResponseEntity<?> updateFeedsStatus(@RequestBody Map<String, Object> payload,
                                               @SessionAttribute(name="loginUser", required = false) LoginUser loginUser) { // 세션 이름과 타입은 이제 정확합니다.
        
        // [최종 수정] 세션 객체의 권한을 명시적으로 확인하는 로직 추가
        if (loginUser == null) {
            return ResponseEntity.status(401).body(Map.of("result", "fail", "message", "로그인이 필요합니다."));
        }

        String userGrade = loginUser.getMbrGrdCd();
        // 관리자 등급('grd_cd_0' 또는 'grd_cd_1')이 아닌 경우, 권한 없음(403 Forbidden) 응답 반환
        if (!"grd_cd_0".equals(userGrade) && !"grd_cd_1".equals(userGrade)) {
            return ResponseEntity.status(403).body(Map.of("result", "fail", "message", "이 작업을 수행할 권한이 없습니다."));
        }
        
        // 권한 확인이 끝난 후에야 실제 로직 수행
        String adminUserNo = loginUser.getUserNo(); 
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