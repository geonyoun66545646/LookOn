// [전체 코드] ks55team02/customer/feed/controller/FeedRestController.java
package ks55team02.customer.feed.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import ks55team02.customer.feed.domain.FeedComment;
import ks55team02.customer.feed.service.FeedService;
import ks55team02.customer.login.domain.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/customer/api/feeds")
@RequiredArgsConstructor
@Slf4j
public class FeedRestController {

    private final FeedService feedService;
    private static final int PAGE_SIZE = 12;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> selectFeedsForScroll(@RequestParam(value = "page", defaultValue = "1") int page) {
        Map<String, Object> result = feedService.selectFeedList(null, page, PAGE_SIZE);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/next")
    public ResponseEntity<List<ks55team02.customer.feed.domain.Feed>> selectNextFeeds(@RequestParam("currentFeedCrtDt") String currentFeedCrtDt, @RequestParam(value = "limit", defaultValue = "3") int limit, @RequestParam(name = "context", defaultValue = "all") String context, @RequestParam(name = "userNo", required = false) String userNo) {
        List<ks55team02.customer.feed.domain.Feed> nextFeedList = feedService.selectNextFeedList(currentFeedCrtDt, limit, context, userNo);
        if (nextFeedList == null || nextFeedList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(nextFeedList);
    }
    
    @GetMapping("/my-feed")
    public ResponseEntity<Map<String, Object>> selectMyFeeds(@RequestParam(name = "page", defaultValue = "1") int page, @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser) {
        if (loginUser == null) return ResponseEntity.status(401).build();
        String userNo = loginUser.getUserNo();
        Map<String, Object> result = feedService.selectFeedList(userNo, page, PAGE_SIZE);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user-feed/{userNo}")
    public ResponseEntity<Map<String, Object>> selectUserFeeds(@PathVariable("userNo") String userNo, @RequestParam(name = "page", defaultValue = "1") int page) {
        Map<String, Object> result = feedService.selectFeedList(userNo, page, PAGE_SIZE);
        return ResponseEntity.ok(result);
    }
    
    // [수정] 해시태그 파라미터 추가 및 파일 파라미터명 변경
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Void> insertFeed(
            @RequestParam(value = "feedCn", required = false) String feedCn,
            @RequestParam(value = "hashtags", required = false) String hashtags,
            @RequestParam(value = "newImageFiles", required = false) List<MultipartFile> imageFiles,
            @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser) {
        
        if (loginUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        boolean isEmptyContent = (feedCn == null || feedCn.trim().isEmpty());
        boolean isEmptyImages = (imageFiles == null || imageFiles.stream().allMatch(MultipartFile::isEmpty));
        if (isEmptyContent && isEmptyImages) return ResponseEntity.badRequest().build();
        
        try {
            feedService.insertFeed(feedCn, hashtags, imageFiles, loginUser);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("피드 작성 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =======================================================
    // [신규] 피드 수정 API
    // =======================================================
    @PostMapping(value = "/edit/{feedSn}", consumes = {"multipart/form-data"})
    public ResponseEntity<Void> updateFeed(
            @PathVariable("feedSn") String feedSn,
            @RequestParam(value = "feedCn", required = false) String feedCn,
            @RequestParam(value = "hashtags", required = false) String hashtags,
            @RequestParam(value = "deleteImageSns", required = false) List<String> deleteImageSns,
            @RequestParam(value = "newImageFiles", required = false) List<MultipartFile> newImageFiles,
            @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser) {
        
        if (loginUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            boolean isSuccess = feedService.updateFeed(feedSn, feedCn, hashtags, deleteImageSns, newImageFiles, loginUser);
            if (isSuccess) {
                return ResponseEntity.ok().build(); // 수정 성공
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 권한 없음
            }
        } catch (Exception e) {
            log.error("피드 수정 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // =======================================================
    // [신규] 피드 삭제 API
    // =======================================================
    @DeleteMapping("/{feedSn}")
    public ResponseEntity<Void> deleteFeed(
            @PathVariable("feedSn") String feedSn,
            @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser) {

        if (loginUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        boolean isSuccess = feedService.deleteFeed(feedSn, loginUser.getUserNo());
        if (isSuccess) {
            return ResponseEntity.noContent().build(); // 삭제 성공 (204 No Content)
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 권한 없음
        }
    }

    // 이하 댓글, 좋아요 관련 API는 기존과 동일
    @PostMapping("/{feedSn}/like")
    public ResponseEntity<Map<String, Object>> addLike(@PathVariable String feedSn, @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser) {
        if (loginUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            Map<String, Object> result = feedService.addLike(feedSn, loginUser.getUserNo());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("좋아요 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{feedSn}/comments")
    public ResponseEntity<?> addComment(@PathVariable String feedSn, @RequestParam("commentText") String commentText, @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser) {
        if (loginUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (commentText == null || commentText.trim().isEmpty()) return ResponseEntity.badRequest().body("댓글 내용이 없습니다.");
        try {
            FeedComment newComment = feedService.addComment(feedSn, commentText, loginUser.getUserNo());
            return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
        } catch (Exception e) {
            log.error("댓글 작성 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/comments/{commentSn}")
    public ResponseEntity<Void> deleteComment(@PathVariable String commentSn, @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser) {
        if (loginUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean isDeleted = feedService.deleteComment(commentSn, loginUser.getUserNo());
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
    
    @PatchMapping("/comments/{commentSn}")
    public ResponseEntity<?> updateComment(@PathVariable String commentSn, @RequestParam("commentText") String commentText, @SessionAttribute(name = "loginUser", required = false) LoginUser loginUser) {
        if (loginUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (commentText == null || commentText.trim().isEmpty()) return ResponseEntity.badRequest().body("댓글 내용이 없습니다.");
        try {
            FeedComment updatedComment = feedService.updateComment(commentSn, commentText, loginUser.getUserNo());
            if (updatedComment != null) {
                return ResponseEntity.ok(updatedComment);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            log.error("댓글 수정 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}