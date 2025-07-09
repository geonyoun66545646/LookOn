package ks55team02.customer.feed.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import jakarta.servlet.http.HttpSession;
import ks55team02.customer.feed.domain.Feed;
import ks55team02.customer.feed.service.FeedService;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.customer.login.domain.UserInfoResponse;
import ks55team02.customer.login.service.UserInfoService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/customer/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final UserInfoService userInfoService;
    private static final int PAGE_SIZE = 12; // 한 페이지에 보여줄 피드 개수

    // 피드 목록 조회
    @GetMapping("/feedList")
    public String selectFeedList(Model model) {
        Map<String, Object> result = feedService.selectFeedList(1, PAGE_SIZE);
        model.addAttribute("feedList", result.get("feedList"));
        model.addAttribute("hasNext", result.get("hasNext"));
        return "customer/feed/feedList";
    }
    
    // 피드 상세 조회
    @GetMapping("/feedDetail/{feedSn}")
    public String selectFeedDetail(@PathVariable String feedSn, Model model) {
        Feed feed = feedService.selectFeedDetail(feedSn);
        model.addAttribute("feed", feed);
        return "customer/feed/feedDetail";
    }
    
    // 마이 피드 조회
    @GetMapping("/feedListByMe")
    public String selectFeedListByMe(HttpSession session, Model model) {
    	// 세션 처리
    	Object sessionObj = session.getAttribute("loginUser");
    	if(sessionObj == null) {
    		return "redirect:/customer/login/login?redirectUrl=/customer/feed/feedListByMe";
    	}
    	// 세션 처리 끝
    	LoginUser loginUser = (LoginUser) sessionObj;
    	
    	
    	String userNo = loginUser.getUserNo();
    	UserInfoResponse userInfo = userInfoService.getUserInfo(userNo);
    	Map<String, Object> feedData = feedService.selectFeedListByMe(userNo, 1, PAGE_SIZE);
    	
    	model.addAttribute("userInfo", userInfo);
    	model.addAttribute("feedList", feedData.get("feedList"));
    	model.addAttribute("hasNext", feedData.get("hasNext"));
    	
    	return "customer/feed/feedListByMe";
    }
    
    // 마이 피드 조회 무한스크롤
    @GetMapping("/myFeed")
    public String getMyFeeds(
    		@RequestParam(name = "page", defaultValue = "1") int page,
    		@SessionAttribute(name = "loginUser", required = false) LoginUser loginUser,
    		Model model) {
    	
        if (loginUser == null) {
            // 로그인하지 않은 사용자의 요청은 처리하지 않음
            return "error/401"; // 혹은 적절한 에러 페이지
        }
        
        String userNo = loginUser.getUserNo();
        Map<String, Object> feedData = feedService.selectFeedListByMe(userNo, page, PAGE_SIZE);

        model.addAttribute("feedList", feedData.get("feedList"));
    	
    	return "customer/feed/feedListByMe :: feedListFragment";
    }
}