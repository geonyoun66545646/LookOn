package ks55team02.customer.login.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.customer.login.domain.UserInfoResponse;
import ks55team02.customer.login.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserInfoApiController {

	private final UserInfoService userInfoService;
	
	@GetMapping("/me")
	public ResponseEntity<?> getMyInfo(HttpSession session){
		
		LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
		
		// 2. 비로그인 상태인지 확인 (가장 중요한 방어 로직)
        if (loginUser == null) {
            log.warn("인증되지 않은 사용자의 정보 요청입니다.");
            // 401 Unauthorized 에러를 반환하여, 클라이언트가 로그인 페이지로 보내도록 유도
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // 3. (다음 단계에서 구현) 로그인된 사용자의 userNo를 서비스에 전달하여 상세 정보 조회
        UserInfoResponse userInfo = userInfoService.getUserInfo(loginUser.getUserNo());
        
        // (방어 로직) 혹시 모를 경우를 대비해, DB에서 조회가 안 된 경우 처리
        if (userInfo == null) {
            log.error("세션은 존재하지만 DB에서 사용자 정보를 찾을 수 없음. userNo: {}", loginUser.getUserNo());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 정보를 처리하는 중 오류가 발생했습니다.");
        }
        return ResponseEntity.ok(userInfo);
	}
}
