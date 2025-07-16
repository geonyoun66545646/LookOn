package ks55team02.customer.feed.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import ks55team02.customer.feed.domain.Feed;
import ks55team02.customer.feed.service.FeedService;
import ks55team02.customer.login.domain.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/customer/feed") // API 요청을 위한 경로
@RequiredArgsConstructor
@Slf4j
public class FeedRestController {

    private final FeedService feedService;
    private static final int PAGE_SIZE = 12;

    // 피드 무한 스크롤
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> selectFeedsForScroll(
                            @RequestParam(value = "page", defaultValue = "1") int page) {
        Map<String, Object> result = feedService.selectFeedList(null, page, PAGE_SIZE);
        return ResponseEntity.ok(result);
    }
    
    // 피드 다음 페이지 무한 스크롤
    @GetMapping("/next")
    public ResponseEntity<List<Feed>> selectNextFeeds(
            @RequestParam("currentFeedCrtDt") String currentFeedCrtDt,
            @RequestParam(value = "limit", defaultValue = "3") int limit,
            @RequestParam(name = "context", defaultValue = "all") String context,
            @RequestParam(name = "userNo", required = false) String userNo) {
    	log.info("### [API CALLED] context: {}, userNo: {}", context, userNo);
        List<Feed> nextFeedList = feedService.selectNextFeedList(currentFeedCrtDt, limit, context, userNo);

        if (nextFeedList == null || nextFeedList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(nextFeedList);
    }
    
    // 마이피드 무한 스크롤
    @GetMapping("/my-feed") // RESTful한 네이밍 컨벤션을 따르는 것을 권장합니다.
    public ResponseEntity<Map<String, Object>> selectMyFeeds(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser) {

        if (loginUser == null) {
            // 로그인되지 않은 사용자는 401 Unauthorized 응답을 반환하여
            // 클라이언트에서 명확하게 로그인 처리를 유도합니다.
            return ResponseEntity.status(401).build();
        }

        String userNo = loginUser.getUserNo();
        
        // 서비스의 반환값을 그대로 ResponseEntity.ok()에 담아 반환하는 기존 스타일을 따릅니다.
        Map<String, Object> result = feedService.selectFeedList(userNo, page, PAGE_SIZE);
        
        return ResponseEntity.ok(result);
    }

    // 남의 마이피드 조회
    @GetMapping("/user-feed/{userNo}")
    public ResponseEntity<Map<String, Object>> selectUserFeeds(
            @PathVariable("userNo") String userNo,
            @RequestParam(name = "page", defaultValue = "1") int page) {

        // 4. "마이피드"와 "타인피드" 모두 동일한 서비스 메소드를 재사용합니다.
        // 이것이 바로 이번 리팩토링의 핵심 목표였습니다.
        Map<String, Object> result = feedService.selectFeedList(userNo, page, PAGE_SIZE);

        return ResponseEntity.ok(result);
    }
    
}