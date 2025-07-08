package ks55team02.customer.login.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import ks55team02.customer.login.domain.Login;
import ks55team02.customer.login.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
@Slf4j
public class LoginController {

	private final LoginService loginService;

    // 로그인 처리를 위한 POST 요청을 받는 메소드
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> loginProcess(@RequestBody Login loginInfo, HttpServletRequest request) {

        // 1. 클라이언트의 IP 주소 가져오기
        String clientIp = request.getRemoteAddr();
        loginInfo.setIpAddress(clientIp);

        // 250708 리다이렉트 추가
        log.info("로그인 시도 => ID: {}, IP: {}, 복귀주소: {}", loginInfo.getUserLgnId(), clientIp, loginInfo.getRedirectUrl());
        
        // 2. LoginService를 호출하여 로그인 로직 실행
        Login userInfo = loginService.login(loginInfo, request);

        // 3. 결과를 담을 Map 객체 생성
        Map<String, Object> response = new HashMap<>();

        // 4. 로그인 결과에 따라 분기 처리
        if (userInfo != null) {
            // 4-1. 계정 상태에 따른 상세 분기
            if (userInfo.getAcntLockRmvTm() != null && userInfo.getAcntLockRmvTm().isAfter(java.time.LocalDateTime.now())) {
                // 계정이 잠겨있는 경우
                response.put("status", "locked");
                response.put("message", "계정이 잠겨있습니다. 잠금 해제 시간: " + userInfo.getAcntLockRmvTm());
                return ResponseEntity.ok(response);
            } else if (!"활동".equals(userInfo.getUserStatus())) {
                // 계정이 비활성(휴면/탈퇴) 상태인 경우
                response.put("status", "inactive");
                response.put("message", "휴면 또는 탈퇴 상태의 계정입니다.");
                return ResponseEntity.ok(response);
            }

            // 4-2. 최종 로그인 성공
            response.put("status", "success");
            response.put("message", "로그인에 성공하였습니다.");
            response.put("redirectUrl", loginInfo.getRedirectUrl()); // 리다이렉트용 주소 정보 담기
            log.info("세션 생성 완료: {}", userInfo.getUserLgnId());

        } else {
            // 4-3. 로그인 실패 (아이디/비밀번호 불일치 등)
            response.put("status", "fail");
            response.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // 5. 최종 응답을 JSON 형태로 반환
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
    	session.invalidate();
    	
    	log.info("========== 로그아웃 메소드 실행됨 ==========");
    	return "redirect:/"; 
    }
}
