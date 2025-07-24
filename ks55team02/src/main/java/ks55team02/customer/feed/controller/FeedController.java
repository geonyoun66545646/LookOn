package ks55team02.customer.feed.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import ks55team02.customer.feed.domain.Feed;
import ks55team02.customer.feed.service.FeedService;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.customer.login.domain.UserInfoResponse;
import ks55team02.customer.login.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/customer/feed")
@RequiredArgsConstructor
@Slf4j
public class FeedController {

	private final FeedService feedService;
	private final UserInfoService userInfoService;
	private static final int PAGE_SIZE = 12;

	@GetMapping("/feedList")
	public String selectFeedList(@RequestParam(name = "tab", defaultValue = "discover") String tab,
			@RequestParam(name = "sort", defaultValue = "latest") String sort,
			@SessionAttribute(name = "loginUser", required = false) LoginUser loginUser, Model model) {

		int currentPage = 1;
		Map<String, Object> feedData;

		if ("following".equals(tab)) {
			if (loginUser == null) {
				model.addAttribute("feedList", Collections.emptyList());
				model.addAttribute("hasNext", false);
				model.addAttribute("totalCount", 0);
				model.addAttribute("needsLogin", true);
			} else {
				String followerUserNo = loginUser.getUserNo();
				feedData = feedService.selectFollowingFeedList(followerUserNo, currentPage, PAGE_SIZE);
				model.addAllAttributes(feedData);
			}
		} else {
			feedData = feedService.selectFeedList(null, currentPage, PAGE_SIZE, sort);
			model.addAllAttributes(feedData);
		}

		model.addAttribute("loginUser", loginUser);
		model.addAttribute("activeTab", tab);
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("currentSort", sort);

		return "customer/feed/feedList";
	}

	@GetMapping("/feedDetail/{feedSn}")
	public String selectFeedDetail(@PathVariable String feedSn,
			@RequestParam(name = "context", defaultValue = "all") String context,
			@RequestParam(name = "userNo", required = false) String userNo,
			@SessionAttribute(name = "loginUser", required = false) LoginUser loginUser, Model model) {

		Feed feed = feedService.selectFeedDetail(feedSn);
		if (feed == null) {
			return "redirect:/customer/feed/feedList";
		}
		model.addAttribute("feed", feed);
		model.addAttribute("context", context);
		model.addAttribute("userNo", userNo);
		model.addAttribute("loginUser", loginUser);

		return "customer/feed/feedDetail";
	}

	@GetMapping("/feedListByUserNo")
	public String selectFeedListByUserNo(@SessionAttribute(name = "loginUser", required = false) LoginUser loginUser,
			Model model) {

		if (loginUser != null) {
			String userNo = loginUser.getUserNo();
			UserInfoResponse userInfo = userInfoService.getUserInfo(userNo);
			model.addAttribute("userInfo", userInfo);
			model.addAttribute("loginUserNo", userNo);
		}

		return "customer/feed/feedListByUserNo";
	}

	@GetMapping("/feedListByUserNo/{userNo}")
	public String viewUserFeedPage(@PathVariable("userNo") String userNo,
			@SessionAttribute(name = "loginUser", required = false) LoginUser loginUser, Model model) {
		UserInfoResponse userInfo = userInfoService.getUserInfo(userNo);
		model.addAttribute("userInfo", userInfo);

		if (loginUser != null) {
			model.addAttribute("loginUserNo", loginUser.getUserNo());
		} else {
			model.addAttribute("loginUserNo", "");
		}
		return "customer/feed/feedListByUserNo";
	}

	@GetMapping("/feedWrite")
	public String getFeedWritePage(@SessionAttribute(name = "loginUser", required = false) LoginUser loginUser,
			Model model) {

		if (loginUser != null) {
			UserInfoResponse userInfo = userInfoService.getUserInfo(loginUser.getUserNo());
			model.addAttribute("userInfo", userInfo);
		}
		model.addAttribute("loginUser", loginUser);
		return "customer/feed/feedWrite";
	}

	@GetMapping("/edit/{feedSn}")
	public String getFeedEditPage(@PathVariable("feedSn") String feedSn,
			@SessionAttribute(name = "loginUser", required = false) LoginUser loginUser, Model model) {

		if (loginUser != null) {
			Feed feed = feedService.selectFeedDetail(feedSn);

			if (feed == null || !feed.getWrtrUserNo().equals(loginUser.getUserNo())) {
				log.warn("잘못된 수정 접근: feedSn={}, accessor={}", feedSn, loginUser.getUserNo());
				return "redirect:/customer/feed/feedList";
			}
			UserInfoResponse userInfo = userInfoService.getUserInfo(loginUser.getUserNo());
			model.addAttribute("userInfo", userInfo);
			model.addAttribute("feed", feed);
		}

		model.addAttribute("loginUser", loginUser);
		return "customer/feed/feedWrite";
	}
}