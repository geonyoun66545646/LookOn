package ks55team02.customer.feed.controller;

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

    @GetMapping("/feedList")
    public String selectFeedList(
            @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser,
            Model model) {
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("showFab", true);
        return "customer/feed/feedList";
    }
     
	 @GetMapping("/feedDetail/{feedSn}")
	 public String selectFeedDetail(@PathVariable String feedSn, 
			 			@RequestParam(name = "context", defaultValue = "all") String context,
			 			@RequestParam(name = "userNo", required = false) String userNo,
                        @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser,
			 			Model model) {
		 
	     Feed feed = feedService.selectFeedDetail(feedSn);
	     model.addAttribute("feed", feed);
	     model.addAttribute("context", context);
	     model.addAttribute("userNo", userNo);
         // [핵심 추가] 현재 로그인 사용자 정보를 모델에 추가
         model.addAttribute("loginUser", loginUser);
	     model.addAttribute("showFab", true);

	     return "customer/feed/feedDetail";
	 }
	    
    @GetMapping("/feedListByUserNo")
    public String selectFeedListByUserNo(
            @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser, 
            Model model) {
    	if(loginUser == null) {
    		return "redirect:/customer/login/login?redirectUrl=/customer/feed/feedListByUserNo";
    	}
    	String userNo = loginUser.getUserNo();
    	UserInfoResponse userInfo = userInfoService.getUserInfo(userNo);
    	model.addAttribute("userInfo", userInfo);
    	model.addAttribute("showFab", true);
    	model.addAttribute("loginUserNo", userNo);
    	return "customer/feed/feedListByUserNo";
    }
    
    @GetMapping("/feedListByUserNo/{userNo}")
    public String userFeedPage(@PathVariable("userNo") String userNo,
    					@SessionAttribute(name = "loginUser", required = false) LoginUser loginUser, 
    					Model model) {
        UserInfoResponse userInfo = userInfoService.getUserInfo(userNo);
        model.addAttribute("userInfo", userInfo);
        model.addAttribute("showFab", true);
        if (loginUser != null) {
            model.addAttribute("loginUserNo", loginUser.getUserNo());
        } else {
            model.addAttribute("loginUserNo", "");
        }
        return "customer/feed/feedListByUserNo";
    }
    
    @GetMapping("/feedWrite")
    public String feedWritePage(
            @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser) {
        if (loginUser == null) {
            return "redirect:/customer/login/login?redirectUrl=/customer/feed/write";
        }
        return "customer/feed/feedWrite";
    }
}