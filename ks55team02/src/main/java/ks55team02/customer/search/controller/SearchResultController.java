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
	 * 클라이언트의 실제 IP 주소를 가져오는 private 헬퍼 메소드
	 * X-REAL-IP 헤더를 최우선으로 고려하며, 다양한 프록시 환경에 대응합니다.
	 */
	private String getClientIp(HttpServletRequest request) {
		String ip = null; // IP 주소를 저장할 변수를 초기화합니다.

		// ⭐ 1. X-REAL-IP 헤더를 가장 먼저 확인합니다. (선생님 지시 반영)
		// 이 헤더는 로드 밸런서/프록시가 실제 클라이언트 IP를 전달할 때 사용됩니다.
		ip = request.getHeader("X-REAL-IP");
		if (ip != null && ip.length() > 0 && !"unknown".equalsIgnoreCase(ip)) {
			return ip; // 유효한 값을 찾으면 바로 반환합니다.
		}

		// 2. X-Forwarded-For 헤더를 확인합니다. (가장 일반적인 프록시/로드밸런서 헤더)
		//    이 헤더에는 여러 IP가 콤마로 구분되어 있을 수 있으므로 첫 번째 IP를 사용합니다.
		ip = request.getHeader("X-Forwarded-For");
		if (ip != null && ip.length() > 0 && !"unknown".equalsIgnoreCase(ip)) {
			// 여러 IP가 있을 경우 (예: "클라이언트IP, 프록시1IP, 프록시2IP"), 가장 앞의 IP를 가져옵니다.
			int commaIndex = ip.indexOf(',');
			if (commaIndex > 0) {
				return ip.substring(0, commaIndex).trim();
			}
			return ip.trim(); // 콤마가 없으면 전체를 반환합니다.
		}

		// 3. 다른 일반적인 프록시 관련 헤더들을 순차적으로 확인합니다.
		//    이 헤더들은 특정 프록시 소프트웨어에서 사용될 수 있습니다.
		ip = request.getHeader("Proxy-Client-IP");
		if (ip != null && ip.length() > 0 && !"unknown".equalsIgnoreCase(ip)) {
			return ip;
		}

		ip = request.getHeader("WL-Proxy-Client-IP");
		if (ip != null && ip.length() > 0 && !"unknown".equalsIgnoreCase(ip)) {
			return ip;
		}

		ip = request.getHeader("HTTP_CLIENT_IP");
		if (ip != null && ip.length() > 0 && !"unknown".equalsIgnoreCase(ip)) {
			return ip;
		}

		ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		if (ip != null && ip.length() > 0 && !"unknown".equalsIgnoreCase(ip)) {
			return ip;
		}

		// 4. 모든 헤더에서 유효한 IP를 찾지 못하면, 최종적으로 request.getRemoteAddr()을 사용합니다.
		//    이것은 요청을 보낸 최종 클라이언트(또는 마지막 프록시)의 IP 주소입니다.
		return request.getRemoteAddr();
	}
}