package ks55team02.admin.adminpage.boardadmin.commentmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/boardadmin")
public class CommentManagementController {

	//댓글 관리
	@GetMapping("/commentManagement")
	public String inquiryAdminController() {
		
		return "admin/adminpage/boardadmin/commentManagement";
	}
}
