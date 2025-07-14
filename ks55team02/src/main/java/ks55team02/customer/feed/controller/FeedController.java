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
    public String selectFeedList(
            @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser,
            Model model) {
        
        // 로그인 사용자 정보를 모델에 추가하여, 뷰에서 로그인/로그아웃 상태에 따른 UI 처리가 가능하도록 함.
        model.addAttribute("loginUser", loginUser);

        // feedList와 hasNext는 더 이상 모델에 추가하지 않습니다.
        
        return "customer/feed/feedList";
    }
	     
	 // 2. 피드 상세 조회 (selectFeedDetail)
	 // - 특별한 세션 처리가 없음. 로그인 여부와 관계없이 동작.
	 @GetMapping("/feedDetail/{feedSn}")
	 public String selectFeedDetail(@PathVariable String feedSn, Model model) {
	     Feed feed = feedService.selectFeedDetail(feedSn);
	     model.addAttribute("feed", feed);
	     return "customer/feed/feedDetail";
	 }
	    
    // 마이 피드 조회
    @GetMapping("/feedListByMe")
    public String selectFeedListByMe(
            @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser, 
            Model model) {
    	
    	if(loginUser == null) {
            // @SessionAttribute(required = false)를 사용하면 세션이 없을 때 null이 주입됩니다.
            // 기존과 같이 로그인 페이지로 리다이렉트합니다.
    		return "redirect:/customer/login/login?redirectUrl=/customer/feed/feedListByMe";
    	}
    	
    	String userNo = loginUser.getUserNo();
    	UserInfoResponse userInfo = userInfoService.getUserInfo(userNo);
    	
    	model.addAttribute("userInfo", userInfo);
    	
        // feedList와 hasNext는 더 이상 모델에 추가하지 않습니다.
    	
    	return "customer/feed/feedListByMe";
    }
    

}