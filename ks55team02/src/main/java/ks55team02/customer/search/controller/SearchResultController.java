package ks55team02.customer.search.controller;

import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.customer.search.domain.Search;
import ks55team02.customer.search.service.SearchService;
import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class SearchResultController {

	private final SearchService searchService;

	@GetMapping("/searchResult")
	public String showSearchResults(
			@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword, HttpSession session,
			HttpServletRequest request, Model model) {

		// (수정) searchData 변수 선언을 메소드 최상단으로 옮깁니다.
		Search searchData;

		if (keyword == null || keyword.trim().isEmpty()) {
			searchData = new Search(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
		} else {
			LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
			String userNo = (loginUser != null) ? loginUser.getUserNo() : null;
			String ipAddress = getClientIp(request);


			// 이제 이 호출은 SearchService 인터페이스 수정 후 정상 동작합니다.
			searchData = searchService.searchAll(keyword, userNo, ipAddress);
		}

		model.addAttribute("searchResult", searchData);
		model.addAttribute("keyword", keyword);

		return "customer/search/searchResult";
	}

	/**
	 * (추가 3) 클라이언트의 IP 주소를 가져오는 private 헬퍼 메소드
	 */
	private String getClientIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
}