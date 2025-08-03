package ks55team02.admin.adminpage.inquiryadmin.reports.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ks55team02.admin.adminpage.inquiryadmin.reports.service.AdminContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/admin/content")
@RequiredArgsConstructor
public class AdminContentApiController {

    // [중요] 각 콘텐츠를 조회하는 로직을 담을 새로운 서비스입니다.
    private final AdminContentService adminContentService;

    @GetMapping("/posts/{contentId}")
    public ResponseEntity<Object> getPost(@PathVariable String contentId) {
        log.info("관리자용 원본 게시글 조회 API 호출. ID: {}", contentId);
        Object postData = adminContentService.getOriginalPost(contentId);
        return ResponseEntity.ok(postData);
    }

    @GetMapping("/comments/{contentId}")
    public ResponseEntity<Object> getComment(@PathVariable String contentId) {
        log.info("관리자용 원본 댓글 조회 API 호출. ID: {}", contentId);
        Object commentData = adminContentService.getOriginalComment(contentId);
        return ResponseEntity.ok(commentData);
    }

    @GetMapping("/products/{contentId}")
    public ResponseEntity<Object> getProduct(@PathVariable String contentId) {
        log.info("관리자용 원본 상품 조회 API 호출. ID: {}", contentId);
        Object productData = adminContentService.getOriginalProduct(contentId);
        return ResponseEntity.ok(productData);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Object> getUser(@PathVariable String userId) {
        log.info("관리자용 원본 사용자 조회 API 호출. ID: {}", userId);
        Object userData = adminContentService.getOriginalUser(userId);
        return ResponseEntity.ok(userData);
    }
}