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
         // [수정] 피드가 없거나 삭제된 경우 목록으로 리다이렉트
         if (feed == null) {
             return "redirect:/customer/feed/feedList";
         }
	     model.addAttribute("feed", feed);
	     model.addAttribute("context", context);
	     model.addAttribute("userNo", userNo);
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
        model.addAttribute("loginUserNo", loginUser.getUserNo());
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
            @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser,
            Model model) {
        if (loginUser == null) {
            return "redirect:/customer/login/login?redirectUrl=/customer/feed/feedWrite";
        }
        UserInfoResponse userInfo = userInfoService.getUserInfo(loginUser.getUserNo());
        model.addAttribute("userInfo", userInfo); 
        model.addAttribute("loginUser", loginUser);
        return "customer/feed/feedWrite";
    }
    
    // =======================================================
    // [신규] 피드 수정 페이지 이동 컨트롤러
    // =======================================================
    @GetMapping("/edit/{feedSn}")
    public String feedEditPage(@PathVariable("feedSn") String feedSn,
                               @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser,
                               Model model) {
        if (loginUser == null) {
            return "redirect:/customer/login/login?redirectUrl=/customer/feed/edit/" + feedSn;
        }

        Feed feed = feedService.selectFeedDetail(feedSn);

        // 피드가 존재하지 않거나, 현재 로그인한 사용자가 작성자가 아닌 경우 접근 차단
        if (feed == null || !feed.getWrtrUserNo().equals(loginUser.getUserNo())) {
            log.warn("잘못된 수정 접근: feedSn={}, accessor={}", feedSn, loginUser.getUserNo());
            return "redirect:/customer/feed/feedList";
        }
        UserInfoResponse userInfo = userInfoService.getUserInfo(loginUser.getUserNo());
        model.addAttribute("userInfo", userInfo);
        model.addAttribute("feed", feed);
        model.addAttribute("loginUser", loginUser);
        return "customer/feed/feedWrite"; // 작성 페이지(feedWrite.html) 재사용
    }
}