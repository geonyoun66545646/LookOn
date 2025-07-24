package ks55team02.admin.adminpage.boardadmin.feedmanagement.controller;

import ks55team02.admin.adminpage.boardadmin.feedmanagement.domain.AdminFeed;
import ks55team02.admin.adminpage.boardadmin.feedmanagement.service.FeedManagementService;
import ks55team02.admin.common.domain.Pagination;
import ks55team02.customer.login.domain.LoginUser;
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
@RequestMapping("/adminpage/boardadmin") // 클래스 레벨의 공통 경로는 /adminpage/boardadmin
@RequiredArgsConstructor
@Slf4j
public class FeedManagementController {

	private final FeedManagementService feedManagementService;

	// '피드 관리' 메뉴 클릭 시 호출될 URL: /adminpage/boardadmin/feedManagement
	@GetMapping("/feedManagement")
	public String viewFeedList(@ModelAttribute AdminFeed searchCriteria, Model model) {

		if (searchCriteria.getFilterConditions() == null || searchCriteria.getFilterConditions().isEmpty()) {
			searchCriteria.setFilterConditions(Arrays.asList("normal"));
		}
		if (searchCriteria.getSortOrder() == null || searchCriteria.getSortOrder().isEmpty()) {
			searchCriteria.setSortOrder("crtDtDesc");
		}
		int intPageSize = searchCriteria.getPsizeAsInt();
		searchCriteria.setPageSize(intPageSize);
		int totalCount = feedManagementService.selectFeedCount(searchCriteria);
		Pagination pagination = new Pagination(totalCount, searchCriteria);
		searchCriteria.setOffset(pagination.getLimitStart());
		List<AdminFeed> feedList = feedManagementService.selectFeedList(searchCriteria);
		model.addAttribute("title", "피드 관리");
		model.addAttribute("feedList", feedList);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("pagination", pagination);
		model.addAttribute("searchCriteria", searchCriteria);

		return "admin/adminpage/boardadmin/feedmanagement/adminFeedList";
	}

	// JS에서 Ajax로 목록을 검색할 때 호출될 URL: /adminpage/boardadmin/feedManagement/feedSearch
	@GetMapping("/feedManagement/feedSearch")
	public String searchFeedList(@ModelAttribute AdminFeed searchCriteria, Model model) {

		int intPageSize = searchCriteria.getPsizeAsInt();
		searchCriteria.setPageSize(intPageSize);
		int totalCount = feedManagementService.selectFeedCount(searchCriteria);
		Pagination pagination = new Pagination(totalCount, searchCriteria);
		searchCriteria.setOffset(pagination.getLimitStart());
		List<AdminFeed> feedList = feedManagementService.selectFeedList(searchCriteria);
		model.addAttribute("feedList", feedList);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("pagination", pagination);
		model.addAttribute("searchCriteria", searchCriteria);

		return "admin/adminpage/boardadmin/feedmanagement/adminFeedList :: feedListFragment";
	}

	// JS에서 피드 상태를 변경할 때 호출될 URL: /adminpage/boardadmin/feedManagement/updateStatus
	@PostMapping("/feedManagement/updateStatus")
	@ResponseBody
	public ResponseEntity<?> updateFeedsStatus(@RequestBody Map<String, Object> payload,
			@SessionAttribute(name = "loginUser", required = false) LoginUser loginAdmin) {

		if (loginAdmin == null) {
			return ResponseEntity.status(401).body(Map.of("result", "fail", "message", "관리자 로그인이 필요합니다."));
		}
		String userGrade = loginAdmin.getMbrGrdCd();
		if (!"grd_cd_0".equals(userGrade) && !"grd_cd_1".equals(userGrade)) {
			return ResponseEntity.status(403).body(Map.of("result", "fail", "message", "이 작업을 수행할 권한이 없습니다."));
		}

		String adminUserNo = loginAdmin.getUserNo();
		List<String> feedSns = (List<String>) payload.get("feedSns");
		String action = (String) payload.get("action");

		try {
			if ("hide".equals(action)) {
				feedManagementService.updateFeedsToHidden(feedSns, adminUserNo);
			} else if ("restore".equals(action)) {
				feedManagementService.updateFeedsToRestored(feedSns);
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