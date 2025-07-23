package ks55team02.admin.adminpage.boardadmin.boardmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/boardadmin")
public class BoardManagementController {

	// 게시판 관리자
	@GetMapping("/boardManagement")
	public String boardAdminBoardController() {
		
		return "admin/adminpage/boardadmin/boardManagement";
	}
	
	// 피드 관리 (이 메소드는 이제 FeedManagementController로 이동했으므로 여기서는 제거되거나,
    // 혹은 사이드바 링크용으로 남겨두고 feedList로 리다이렉션 할 수 있습니다. 
    // 지금은 우선 원래의 기본 형태만 남겨두겠습니다.)
	@GetMapping("/feedManagement")
	public String feedAdminController() {
		// 명확한 목록 페이지로 리다이렉트
		return "redirect:/adminpage/boardadmin/feedmanagement/feedList";
	}
}