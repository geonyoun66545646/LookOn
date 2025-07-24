package ks55team02.admin.adminpage.useradmin.userlist.controller;


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

import ks55team02.admin.adminpage.useradmin.userlist.domain.UserList;
import ks55team02.admin.adminpage.useradmin.userlist.servic.UserListService;
import ks55team02.admin.common.domain.Pagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 페이지 내 회원 목록 관리 요청을 처리하는 컨트롤러 클래스입니다.
 * 회원 목록 조회, 검색, 페이징 및 회원 상태 변경 기능을 제공합니다.
 */
@Controller
@RequestMapping("/adminpage/useradmin")
@RequiredArgsConstructor
@Slf4j
public class UserListController {
	
	private final UserListService userListService;

	/**
	 * 회원 목록 페이지의 최초 로딩을 처리하는 GET 요청 핸들러 메소드입니다.
	 * 검색 조건에 맞는 전체 회원 수를 조회하고, 페이징 정보를 계산하여 회원 목록을 가져와 뷰에 전달합니다.
	 *
	 * @param searchCriteria 검색 조건을 담는 {@link UserList} DTO. `@ModelAttribute`를 통해 HTTP 요청 파라미터가 자동으로 바인딩됩니다.
	 * @param model          뷰로 데이터를 전달할 Spring UI Model 객체
	 * @return               회원 목록 페이지의 뷰 경로 (templates/admin/adminpage/useradmin/userList.html)
	 */
	@GetMapping("/userList")
	public String useradminUserListController(@ModelAttribute UserList searchCriteria, Model model) {

		// 1. 검색 조건에 맞는 전체 회원 수를 조회
		int totalCount = userListService.getUserCount(searchCriteria);

		// 2. 전체 회원 수와 검색 조건을 기반으로 CustomerPagination 객체를 생성
		Pagination pagination = new Pagination(totalCount, searchCriteria);

		// 3. 페이징 처리를 위해 DB 쿼리에 필요한 'offset'을 설정
		searchCriteria.setOffset(pagination.getLimitStart());

		// 4. 설정된 조건에 따라 현재 페이지에 표시될 회원 목록을 조회
		List<UserList> userList = userListService.getUserList(searchCriteria);

		// 5. Model에 데이터 추가
		model.addAttribute("title", "회원목록");
		model.addAttribute("userList", userList);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("pagination", pagination);
		model.addAttribute("searchCriteria", searchCriteria);

		return "admin/adminpage/useradmin/userList";
	}
	
	/**
	 * 선택된 여러 회원의 상태를 일괄적으로 변경하는 REST API 엔드포인트입니다.
	 * 이 메소드는 AJAX 요청을 통해 호출됩니다.
	 *
	 * @param requestData 변경할 회원 번호 목록(`userNos`)과 변경할 상태 값(`status`)을 담은 JSON 형태의 Map 객체
	 * @return            처리 성공/실패 여부와 메시지를 담은 {@link ResponseEntity}
	 */
	@PostMapping("/updateUserStatus")
	@ResponseBody
	public ResponseEntity<?> updateUserStatus(@RequestBody Map<String, Object> requestData) {

		List<String> userNos = (List<String>) requestData.get("userNos");
		String status = (String) requestData.get("status");
        
        try {
            userListService.updateUserStatus(userNos, status);
            return ResponseEntity.ok(Map.of("result", "success", "message", "회원 상태가 성공적으로 변경되었습니다."));
        
        } catch (Exception e) {
            log.error("상태 변경 중 서버 오류 발생", e);
            return ResponseEntity.status(500).body(Map.of("result", "fail", "message", "상태 변경 중 오류가 발생했습니다."));
        }
    }


	/**
	 * 검색 조건(검색어, 회원 상태 등), 정렬, 페이징 등의 조건에 따라 회원 목록을 비동기(AJAX)로 조회하는 핸들러 메소드입니다.
	 * 이 메소드는 Thymeleaf Fragment를 사용하여 페이지의 특정 부분만 업데이트합니다.
	 *
	 * @param searchCriteria 검색 조건을 담는 {@link UserList} DTO
	 * @param model          뷰로 데이터를 전달할 Spring UI Model 객체
	 * @return               갱신될 뷰의 경로 (admin/adminpage/useradmin/userList :: userListFragment)
	 */
	@GetMapping("/userstatussearch")
	public String searchUserList(@ModelAttribute UserList searchCriteria, Model model) {
		loadUserListData(searchCriteria, model); // 공통 로직 호출
	    return "admin/adminpage/useradmin/userList :: userListFragment";
	}
	
	/**
	 * [공통 메서드] 회원 목록 데이터를 조회하고 페이징 정보를 계산하여 모델에 추가하는 중복 로직을 캡슐화한 private 메소드입니다.
	 * 'userList' 및 'userstatussearch' 메소드에서 공통적으로 사용됩니다.
	 *
	 * @param searchCriteria 검색 조건을 담는 {@link UserList} DTO
	 * @param model          뷰에 전달할 데이터를 담는 모델
	 */
	private void loadUserListData(UserList searchCriteria, Model model) {
		// 1. 조건에 맞는 전체 회원 수 조회
		int totalCount = userListService.getUserCount(searchCriteria);
		
		// 2. 페이징 정보 계산
		Pagination pagination = new Pagination(totalCount, searchCriteria);
		searchCriteria.setOffset(pagination.getLimitStart());
		
		// 3. 현재 페이지에 해당하는 회원 목록 조회
		List<UserList> userList = userListService.getUserList(searchCriteria);
		
		// 4. 뷰(Model)에 데이터 추가
		model.addAttribute("userList", userList);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("pagination", pagination);
		model.addAttribute("searchCriteria", searchCriteria);
	}
}
