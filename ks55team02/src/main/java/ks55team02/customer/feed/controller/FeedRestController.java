package ks55team02.customer.feed.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ks55team02.customer.feed.domain.Feed;
import ks55team02.customer.feed.service.FeedService;
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
                            @RequestParam(value = "page", defaultValue = "2") int page) {
        Map<String, Object> result = feedService.selectFeedList(page, PAGE_SIZE);
        return ResponseEntity.ok(result);
    }
    
    // 피드 다음 페이지 무한 스크롤
    @GetMapping("/next")
    public ResponseEntity<Feed> selectNextFeed(
                        @RequestParam("currentFeedCrtDt") String currentFeedCrtDt,
                        @RequestParam("wrtrUserNo") String wrtrUserNo) {
        Feed nextFeed = feedService.selectNextFeed(currentFeedCrtDt, wrtrUserNo);
        if (nextFeed != null) {
            return ResponseEntity.ok(nextFeed);
        }
        return ResponseEntity.noContent().build(); // 다음 피드가 없으면 204 No Content
    }
}