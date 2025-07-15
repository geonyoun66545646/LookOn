package ks55team02.customer.feed.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import ks55team02.customer.feed.domain.Feed;
import ks55team02.customer.feed.service.FeedService;
import ks55team02.customer.login.domain.LoginUser;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/customer/feed") // API 요청을 위한 경로
@RequiredArgsConstructor
public class FeedRestController {

    private final FeedService feedService;
    private static final int PAGE_SIZE = 12;

    // 피드 무한 스크롤
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> selectFeedsForScroll(
                            @RequestParam(value = "page", defaultValue = "1") int page) {
        Map<String, Object> result = feedService.selectFeedList(page, PAGE_SIZE);
        return ResponseEntity.ok(result);
    }
    
    // 피드 다음 페이지 무한 스크롤
    @GetMapping("/next")
    public ResponseEntity<List<Feed>> getNextFeeds(
            @RequestParam("currentFeedCrtDt") String currentFeedCrtDt,
            @RequestParam(value = "limit", defaultValue = "3") int limit) {
        
        // 서비스에 다음 피드 목록을 가져오는 메소드를 호출합니다.
        // 이 메소드는 새로 만들어야 합니다. (아래 서비스 코드 참조)
        List<Feed> nextFeedList = feedService.selectNextFeedList(currentFeedCrtDt, limit);

        if (nextFeedList == null || nextFeedList.isEmpty()) {
            // 다음 피드가 없으면 204 No Content 응답을 보냅니다.
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(nextFeedList);
    }
    
    // 마이피드 무한 스크롤
    @GetMapping("/my-feed") // RESTful한 네이밍 컨벤션을 따르는 것을 권장합니다.
    public ResponseEntity<Map<String, Object>> getMyFeeds(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser) {

        if (loginUser == null) {
            // 로그인되지 않은 사용자는 401 Unauthorized 응답을 반환하여
            // 클라이언트에서 명확하게 로그인 처리를 유도합니다.
            return ResponseEntity.status(401).build();
        }

        String userNo = loginUser.getUserNo();
        
        // 서비스의 반환값을 그대로 ResponseEntity.ok()에 담아 반환하는 기존 스타일을 따릅니다.
        Map<String, Object> result = feedService.selectFeedListByMe(userNo, page, PAGE_SIZE);
        
        return ResponseEntity.ok(result);
    }
    
}