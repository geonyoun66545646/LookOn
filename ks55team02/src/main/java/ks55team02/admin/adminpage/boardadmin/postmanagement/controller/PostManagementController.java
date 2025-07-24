package ks55team02.admin.adminpage.boardadmin.postmanagement.controller;

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

import ks55team02.admin.adminpage.boardadmin.postmanagement.domain.AdminPost;
import ks55team02.admin.adminpage.boardadmin.postmanagement.service.PostManagementService;
import ks55team02.admin.common.domain.Pagination;
import ks55team02.customer.login.domain.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/adminpage/boardadmin/postManagement")
@RequiredArgsConstructor
@Slf4j
public class PostManagementController {

	private final PostManagementService postManagementService;

	// '게시글 관리' 메뉴 클릭 시 호출될 URL: /adminpage/boardadmin/postManagement
	@GetMapping
	public String viewPostList(@ModelAttribute AdminPost searchCriteria, Model model) {

		// 기본 필터/정렬 조건 설정 (피드 관리와 동일)
		if (searchCriteria.getFilterConditions() == null || searchCriteria.getFilterConditions().isEmpty()) {
			searchCriteria.setFilterConditions(Arrays.asList("normal"));
		}
		if (searchCriteria.getSortOrder() == null || searchCriteria.getSortOrder().isEmpty()) {
			searchCriteria.setSortOrder("crtDtDesc");
		}

		// 페이지네이션 처리
		int intPageSize = searchCriteria.getPsizeAsInt();
		searchCriteria.setPageSize(intPageSize);
		int totalCount = postManagementService.selectPostCount(searchCriteria);
		Pagination pagination = new Pagination(totalCount, searchCriteria);
		searchCriteria.setOffset(pagination.getLimitStart());

		// 데이터 조회
		List<AdminPost> postList = postManagementService.selectPostList(searchCriteria);

		// 모델에 데이터 추가
		model.addAttribute("title", "게시글 관리");
		model.addAttribute("postList", postList);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("pagination", pagination);
		model.addAttribute("searchCriteria", searchCriteria);

		return "admin/adminpage/boardadmin/postmanagement/adminPostList";
	}

	// JS에서 Ajax로 목록을 검색할 때 호출될 URL: /adminpage/boardadmin/postManagement/postSearch
	@GetMapping("/postSearch")
	public String searchPostList(@ModelAttribute AdminPost searchCriteria, Model model) {

		// 페이지네이션 처리
		int intPageSize = searchCriteria.getPsizeAsInt();
		searchCriteria.setPageSize(intPageSize);
		int totalCount = postManagementService.selectPostCount(searchCriteria);
		Pagination pagination = new Pagination(totalCount, searchCriteria);
		searchCriteria.setOffset(pagination.getLimitStart());

		// 데이터 조회
		List<AdminPost> postList = postManagementService.selectPostList(searchCriteria);

		// 모델에 데이터 추가
		model.addAttribute("postList", postList);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("pagination", pagination);
		model.addAttribute("searchCriteria", searchCriteria);

		return "admin/adminpage/boardadmin/postmanagement/adminPostList :: postListFragment";
	}

	// JS에서 게시글 상태를 변경할 때 호출될 URL: /adminpage/boardadmin/postManagement/updateStatus
	@PostMapping("/updateStatus")
	@ResponseBody
	public ResponseEntity<?> updatePostsStatus(@RequestBody Map<String, Object> payload,
			@SessionAttribute(name = "loginUser", required = false) LoginUser loginAdmin) {

		// 관리자 로그인 및 권한 확인
		if (loginAdmin == null) {
			return ResponseEntity.status(401).body(Map.of("result", "fail", "message", "관리자 로그인이 필요합니다."));
		}
		String userGrade = loginAdmin.getMbrGrdCd();
		if (!"grd_cd_0".equals(userGrade) && !"grd_cd_1".equals(userGrade)) {
			return ResponseEntity.status(403).body(Map.of("result", "fail", "message", "이 작업을 수행할 권한이 없습니다."));
		}

		List<String> postSns = (List<String>) payload.get("postSns");
		String action = (String) payload.get("action");

		try {
			if ("hide".equals(action)) {
				postManagementService.updatePostsToHidden(postSns);
			} else if ("restore".equals(action)) {
				postManagementService.updatePostsToRestored(postSns);
			} else {
				return ResponseEntity.badRequest().body(Map.of("result", "fail", "message", "알 수 없는 요청입니다."));
			}
			return ResponseEntity.ok(Map.of("result", "success", "message", "게시글 상태가 성공적으로 변경되었습니다."));
		} catch (Exception e) {
			log.error("게시글 상태 변경 중 서버 오류 발생", e);
			return ResponseEntity.status(500).body(Map.of("result", "fail", "message", "처리 중 오류가 발생했습니다."));
		}
	}
}