package ks55team02.customer.login.controller;

import jakarta.servlet.http.HttpSession;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.UserSanction;
import ks55team02.customer.login.domain.LoginUser; // 실제 LoginUser DTO 경로
import ks55team02.customer.login.service.LoginService; // 실제 LoginService 경로
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customer/sanction") // 이 컨트롤러의 기본 경로
@RequiredArgsConstructor
@Slf4j
public class SanctionInfoController {

	private final LoginService loginService; // 제재 정보를 가져올 LoginService 주입

	/**
	 * 계정 이용 제한 안내 페이지를 표시합니다. 이 메소드는 '/customer/sanction/sanctionInfo' GET 요청을
	 * 처리합니다.
	 */
	@GetMapping("/sanctionInfo")
	public String showSanctionInfo(HttpSession session, Model model) {
		log.info("제재 정보 페이지 접근 시도.");

		String userNo = null; // [수정] userNo 변수를 먼저 선언하고 null로 초기화

		// [수정 시작] 세션에서 userNo를 가져오는 로직을 tempSanctionUserNo 우선으로 변경
		LoginUser loginUser = (LoginUser) session.getAttribute("loginUser"); // 기존 로그인 세션
		String tempSanctionUserNo = (String) session.getAttribute("tempSanctionUserNo"); // 임시 제재 사용자 세션

		if (tempSanctionUserNo != null) { // [핵심] tempSanctionUserNo가 있다면, 이 사용자가 제재된 사용자입니다.
			userNo = tempSanctionUserNo;
			log.info("임시 세션(tempSanctionUserNo)을 통해 사용자 {}의 제재 정보 페이지 접근.", userNo);
			// 제재 정보를 표시한 후에는 임시 세션 정보를 제거합니다.
			// 이렇게 함으로써 사용자가 페이지를 새로고침하거나 다시 로그인 시도할 때마다 같은 정보를 받지 않도록 합니다.
			session.removeAttribute("tempSanctionUserNo");
		} else if (loginUser != null) { // tempSanctionUserNo가 없고, loginUser가 있다면, 정상 로그인 사용자입니다.
			userNo = loginUser.getUserNo();
			log.warn("정상 로그인된 사용자 {}가 제재 정보 페이지에 직접 접근했습니다. 의도치 않은 접근일 수 있습니다.", userNo);
			// 이 경우는 정상적으로 로그인된 사용자가 제재 페이지에 온 것이므로, 메인으로 돌려보낼 수 있습니다.
			return "redirect:/";
		} else { // 둘 다 없다면, 비정상적인 접근이거나 세션 만료입니다.
			log.warn("세션에 사용자 정보(loginUser 또는 tempSanctionUserNo)가 없어 로그인 페이지로 리다이렉트합니다.");
			return "redirect:/login";
		}
		// [수정 끝]

		// 2. 사용자 번호(userNo)를 사용하여 최신 제재 상세 정보를 가져옵니다.
		UserSanction latestSanction = loginService.getLatestSanctionByUserNo(userNo);

		if (latestSanction != null) {
			model.addAttribute("sanctionType", latestSanction.getSanctionType());
			model.addAttribute("sanctionReason", latestSanction.getSanctionReason());
			if (latestSanction.getSanctionEndDt() != null) {
				model.addAttribute("sanctionEndDt", latestSanction.getSanctionEndDt()
						.format(java.time.format.DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분")));
			} else {
				model.addAttribute("sanctionEndDt", "영구 정지");
			}
			log.info("사용자 {}에 대한 제재 상세 정보 발견: 유형={}, 사유={}", userNo, latestSanction.getSanctionType(),
					latestSanction.getSanctionReason());
		} else {
			// 제재 기록이 없는데 이 페이지에 도달했다면 (예: 제재가 방금 해제되었거나, 직접 접근 시)
			log.warn("사용자 {}가 제재 정보 페이지에 있지만 활성 제재 기록이 없습니다. 메인 페이지로 리다이렉트합니다.", userNo);
			return "redirect:/"; // 실제 메인 페이지로 리다이렉트
		}

		return "customer/sanction/sanctionInfo"; // 이 반환 문자열이 templates 폴더 아래 HTML 파일 경로와 일치해야 해요.
	}
}