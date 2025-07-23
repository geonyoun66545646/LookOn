package ks55team02.admin.adminpage.boardadmin.feedmanagement.controller;

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
import ks55team02.admin.adminpage.boardadmin.feedmanagement.service.FeedManagementService; // Service 임포트
import ks55team02.admin.common.domain.Pagination;
import ks55team02.admin.login.domain.AdminInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/adminpage/boardadmin/feedmanagement")
@RequiredArgsConstructor
@Slf4j
public class FeedManagementController {

    private final FeedManagementService feedManagementService; // final 필드로 Service 주입

    @GetMapping("/feedList")
    public String feedList(@ModelAttribute AdminFeed searchCriteria, Model model) {
        
        // [수정] 실제 Service를 호출하여 데이터 조회
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
        
        // [수정] 실제 Service를 호출하여 데이터 조회
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
                                               @SessionAttribute(name="loginAdminInfo", required = false) AdminInfo loginAdmin) { // [수정] 세션 이름과 타입을 실제 DTO에 맞게 변경 (세션 이름은 확인 필요)
        
        if (loginAdmin == null) {
            return ResponseEntity.status(401).body(Map.of("result", "fail", "message", "관리자 로그인이 필요합니다."));
        }
        
        // [수정] AdminInfo DTO에 정의된 getUserNo() 메소드를 사용하여 관리자 번호 획득
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