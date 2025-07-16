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
    private static final int PAGE_SIZE = 12; // 한 페이지에 보여줄 피드 개수

	 // 피드 목록 조회
    @GetMapping("/feedList")
    public String selectFeedList(
            @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser,
            Model model) {
        
        // 로그인 사용자 정보를 모델에 추가하여, 뷰에서 로그인/로그아웃 상태에 따른 UI 처리가 가능하도록 함.
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("showFab", true);
        // feedList와 hasNext는 더 이상 모델에 추가하지 않습니다.
        
        return "customer/feed/feedList";
    }
	     
	 // 피드 상세 조회 
	 // - 특별한 세션 처리가 없음. 로그인 여부와 관계없이 동작.
	 @GetMapping("/feedDetail/{feedSn}")
	 public String selectFeedDetail(@PathVariable String feedSn, 
			 			@RequestParam(name = "context", defaultValue = "all") String context,
			 			@RequestParam(name = "userNo", required = false) String userNo,
			 			Model model) {
		 
	     Feed feed = feedService.selectFeedDetail(feedSn);
	     model.addAttribute("feed", feed);
	     model.addAttribute("context", context);
	     model.addAttribute("userNo", userNo);
	     model.addAttribute("showFab", true);
	     return "customer/feed/feedDetail";
	 }
	    
    // 나의 마이 피드 조회
    @GetMapping("/feedListByUserNo")
    public String selectFeedListByUserNo(
            @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser, 
            Model model) {
    	
    	if(loginUser == null) {
            // @SessionAttribute(required = false)를 사용하면 세션이 없을 때 null이 주입됩니다.
            // 기존과 같이 로그인 페이지로 리다이렉트합니다.
    		return "redirect:/customer/login/login?redirectUrl=/customer/feed/feedListByUserNo";
    	}
    	
    	String userNo = loginUser.getUserNo();
    	UserInfoResponse userInfo = userInfoService.getUserInfo(userNo);
    	
    	model.addAttribute("userInfo", userInfo);
    	model.addAttribute("showFab", true);
    	return "customer/feed/feedListByUserNo";
    }
    
    // 타인의 마이 피드 조회
    @GetMapping("/feedListByUserNo/{userNo}")
    public String userFeedPage(@PathVariable("userNo") String userNo,
    					@SessionAttribute(name = "loginUser", required = false) LoginUser loginUser, 
    					Model model) {
        // 경로 변수(userNo)를 이용해 타인의 프로필 정보를 조회합니다.
        UserInfoResponse userInfo = userInfoService.getUserInfo(userNo);
        model.addAttribute("userInfo", userInfo);
        model.addAttribute("showFab", true);

        // "마이피드"와 "타인피드"는 동일한 뷰 파일을 재사용합니다.
        if (loginUser != null) {
            model.addAttribute("loginUserNo", loginUser.getUserNo());
        } else {
            model.addAttribute("loginUserNo", ""); // 비로그인 시 빈 문자열
        }
        
        return "customer/feed/feedListByUserNo";
    }
    
    // 피드 작성
    @GetMapping("/feedWrite")
    public String feedWritePage(
            @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser) {
        
        // 비로그인 사용자는 작성 페이지에 접근할 수 없음
        if (loginUser == null) {
            // 로그인 페이지로 리다이렉트, 로그인 후 작성 페이지로 돌아오도록 URL 전달
            return "redirect:/customer/login/login?redirectUrl=/customer/feed/write";
        }
        
        // feedWrite.html 뷰를 반환합니다.
        return "customer/feed/feedWrite";
    }

}