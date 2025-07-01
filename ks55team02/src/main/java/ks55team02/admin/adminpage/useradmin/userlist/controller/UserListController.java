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

import ks55team02.admin.adminpage.useradmin.userlist.domain.Pagination;
import ks55team02.admin.adminpage.useradmin.userlist.domain.UserList;
import ks55team02.admin.adminpage.useradmin.userlist.servic.UserListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/adminpage/useradmin")
@RequiredArgsConstructor
@Slf4j
public class UserListController {
	
	private final UserListService userListService;

	@GetMapping("/userList")
	public String useradminUserListController(@ModelAttribute UserList searchCriteria, Model model) {

		// 1. 검색 조건에 맞는 전체 회원 수를 조회
		int totalCount = userListService.getUserCount(searchCriteria);

		// 2. 전체 회원 수와 검색 조건을 기반으로 Pagination 객체를 생성
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

	// 회원 상태 변경
	@PostMapping("/updateUserStatus")
	@ResponseBody
	public ResponseEntity<?> updateUserStatus(@RequestBody Map<String, Object> requestData) {

		List<String> userNos = (List<String>) requestData.get("userNos");
		String status = (String) requestData.get("status");
        
        try {
            // 2. Map에서 직접 값을 꺼내서 사용합니다.
            //    userNos는 List<String> 형태로 들어오므로 캐스팅이 필요합니다.

            userListService.updateUserStatus(userNos, status);
            return ResponseEntity.ok(Map.of("result", "success", "message", "회원 상태가 성공적으로 변경되었습니다."));

        } catch (Exception e) {
            log.error("상태 변경 중 서버 오류 발생", e);
            return ResponseEntity.badRequest().body(Map.of("result", "fail", "message", "상태 변경 중 오류가 발생했습니다."));
        }
    }


	// 회원 목록 조회
	@GetMapping("/userstatussearch")
	public String searchUserList(@ModelAttribute UserList searchCriteria, Model model) {

		// 1. 검색 조건에 맞는 전체 회원 수를 조회
	    int totalCount = userListService.getUserCount(searchCriteria);

		// 2. Pagination 객체를 생성하여 페이징 정보를 계산
	    Pagination pagination = new Pagination(totalCount, searchCriteria);

	    // 3. DB 쿼리를 위한 offset 설정
	    searchCriteria.setOffset(pagination.getLimitStart());

        // 4. 페이지에 해당하는 회원 목록 조회
	    List<UserList> userList = userListService.getUserList(searchCriteria);
	    log.info("현재 페이지에 표시될 회원 수: {}", userList.size());

	    // 5. Model에 데이터 추가
	    model.addAttribute("userList", userList);
	    model.addAttribute("totalCount", totalCount);
	    model.addAttribute("pagination", pagination);
	    model.addAttribute("searchCriteria", searchCriteria);


	    return "admin/adminpage/useradmin/userList :: userListFragment";
	}
}
