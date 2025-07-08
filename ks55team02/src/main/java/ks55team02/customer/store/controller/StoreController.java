package ks55team02.customer.store.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import ks55team02.admin.adminpage.useradmin.userlist.domain.UserList;
import ks55team02.customer.store.domain.AppStore;
import ks55team02.customer.store.service.AppStoreService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/store")
@Controller
public class StoreController {
	
	private final AppStoreService appStoreService;

	@GetMapping("/storeMain")
	public String storeMain() {
		
		return "customer/store/storeMainView";
	}
	
	
	private String getCurrentUserId(HttpSession session) {
		// 실제 구현에서는 세션에서 현재 로그인된 사용자의 ID를 가져와야 합니다.
		return "user_no_180"; // 예시
	}
	
	
	@GetMapping("/appStore")
	public String getAppStore(/* @RequestParam("userNo") */String userNo ,Model model, HttpSession session) {
		
		String targetUserNo = getCurrentUserId(session); // 직접 getCurrentUserId의 값을 가져와 사용
		UserList userInfo = appStoreService.getUserInfo(targetUserNo); // 이 값을 서비스 메소드에 전달
		
		// 빈 AppStore 객체 (폼 초기화)
		model.addAttribute("appStore", new AppStore());
		// userInfo 객체를 모델에 추가
		model.addAttribute("userInfo", userInfo);
		model.addAttribute("title", "상점신청 페이지");
		return "customer/store/appStoreView";
	}
	

}
