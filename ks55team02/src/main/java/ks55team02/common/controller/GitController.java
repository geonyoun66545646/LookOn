package ks55team02.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class GitController {

	@GetMapping("/git")
	public String GitControllers(HttpSession session){
		
		if (session != null) {
            session.invalidate(); // 현재 세션을 무효화합니다.
            System.out.println("Git 페이지 접속으로 세션 무효화됨."); // 로그 (개발/디버깅용)
        }
        
	    return "projectinfomation"; // projectinfomation 뷰(HTML 파일)를 반환
	}
}
