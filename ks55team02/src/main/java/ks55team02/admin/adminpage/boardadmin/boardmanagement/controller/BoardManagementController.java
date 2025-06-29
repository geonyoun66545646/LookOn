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
}
