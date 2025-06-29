package ks55team02.admin.adminpage.useradmin.chatlog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adminpage/useradmin")
public class ChatLogController {

	// 채팅 로그
	@GetMapping("/chatLog")
	public String useradminChatLogController() {
				
	return "admin/adminpage/useradmin/chatLog";
	}
}