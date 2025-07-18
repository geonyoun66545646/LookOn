package ks55team02.customer.follow.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import ks55team02.customer.follow.domain.FollowRequest;
import ks55team02.customer.follow.service.FollowService;
import ks55team02.customer.login.domain.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/follows") // 팔로우 관련 API는 /api/v1/follows 로 시작
@RequiredArgsConstructor
@Slf4j
public class FollowController {

	private final FollowService followService;

    /**
     * 현재 로그인 사용자가 특정 사용자를 팔로우하고 있는지 상태를 확인합니다.
     * @param targetUserNo 팔로우 상태를 확인할 대상 사용자의 userNo
     * @param session 현재 로그인한 사용자의 세션 정보
     * @return {"isFollowing": true/false} 형태의 JSON 응답
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getFollowStatus(
            @RequestParam("targetUserNo") String targetUserNo,
            HttpSession session) {
        
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String followerUserNo = loginUser.getUserNo();
        boolean isFollowing = followService.isFollowing(followerUserNo, targetUserNo);
        
        // Collections.singletonMap()은 하나의 키-값 쌍을 가진 Map을 간단히 만드는 방법입니다.
        return ResponseEntity.ok(Collections.singletonMap("isFollowing", isFollowing));
    }

    /**
     * 팔로우를 실행합니다.
     * @param request 팔로우할 대상의 userNo가 담긴 DTO
     * @param session 현재 로그인한 사용자의 세션 정보
     * @return 성공 시 200 OK 응답
     */
    @PostMapping
    public ResponseEntity<Void> follow(@RequestBody FollowRequest request, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String followerUserNo = loginUser.getUserNo();
        String followedUserNo = request.getFollowedUserNo();
        
        followService.follow(followerUserNo, followedUserNo);
        
        return ResponseEntity.ok().build(); // 성공 시 내용 없이 200 OK 응답
    }

    /**
     * 언팔로우를 실행합니다.
     * @param request 언팔로우할 대상의 userNo가 담긴 DTO
     * @param session 현재 로그인한 사용자의 세션 정보
     * @return 성공 시 200 OK 응답
     */
    @DeleteMapping
    public ResponseEntity<Void> unfollow(@RequestBody FollowRequest request, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String followerUserNo = loginUser.getUserNo();
        String followedUserNo = request.getFollowedUserNo();
        
        followService.unfollow(followerUserNo, followedUserNo);
        
        return ResponseEntity.ok().build(); // 성공 시 내용 없이 200 OK 응답
    }
}
