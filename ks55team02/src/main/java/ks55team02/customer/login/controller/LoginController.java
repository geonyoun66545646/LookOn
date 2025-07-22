package ks55team02.customer.login.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Map<String, Object>> loginProcess(@RequestBody Login loginInfo, HttpServletRequest request, HttpSession session) {

    	if (session.getAttribute("loginUser") != null) {
            log.warn("이미 로그인된 사용자의 중복 로그인 시도 차단. IP: {}", request.getRemoteAddr());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "already_logged_in");
            response.put("message", "이미 로그인되어 있습니다.");
            // 이미 로그인된 상태이므로 OK(200) 대신 다른 상태 코드(예: 409 Conflict)를 보내는 것도 좋음
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response); 
        }
    	
        // 1. 클라이언트의 IP 주소 가져오기
        String clientIp = request.getRemoteAddr();
        loginInfo.setIpAddress(clientIp);
        log.info("로그인 시도 => ID: {}, IP: {}, 복귀주소: {}", loginInfo.getUserLgnId(), clientIp, loginInfo.getRedirectUrl());
        
        // 2. LoginService를 호출하여 로그인 로직 실행
        Login userInfo = loginService.login(loginInfo, request);
        
        Map<String, Object> response = new HashMap<>();
        if (userInfo != null) {
        	// 25-07-22 염가은 추가
            // [추가] 계정 상태에 따른 상세 분기 로직을 올바른 if-else if-else 구조로 재구성했습니다.
            //       '제한' 상태가 가장 먼저 체크되도록 하여 우선순위를 줍니다.
            if ("제한".equals(userInfo.getUserStatus())) { // [수정] '제한' 상태 먼저 체크
                response.put("status", "sanctioned");
                response.put("message", "계정이 이용 제한되었습니다.");
                // [수정] 제재 안내 페이지 URL
                response.put("redirectUrl", "/customer/sanction/sanctionInfo"); 

            } else if (userInfo.getAcntLockRmvTm() != null && userInfo.getAcntLockRmvTm().isAfter(java.time.LocalDateTime.now())) {
                // 기존 'locked' 상태
                response.put("status", "locked");
                response.put("message", "계정이 잠겨있습니다. 잠금 해제 시간: " + userInfo.getAcntLockRmvTm());
            } else if (!"활동".equals(userInfo.getUserStatus())) { // '휴면' 또는 '탈퇴' 상태
                // 기존 'inactive' 상태 (이제 '제한'은 위에서 처리되므로 여기는 순수 '휴면', '탈퇴'만 남습니다)
                response.put("status", "inactive");
                response.put("message", "휴면 또는 탈퇴 상태의 계정입니다.");
            } else {
                // 3-2. 최종 로그인 성공 (모든 검사를 통과한 '활동' 상태의 사용자)
                response.put("status", "success");
                response.put("message", "로그인에 성공하였습니다.");
                response.put("redirectUrl", loginInfo.getRedirectUrl());
                log.info("세션 생성 완료: {}", loginInfo.getUserLgnId());
            }
        } else {
            // 3-3. 로그인 실패 (아이디/비밀번호 불일치 또는 관리자 로그인 시도)
            response.put("status", "fail");
            response.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // 4. 최종 응답을 JSON 형태로 반환
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
    	session.invalidate();
    	
    	log.info("========== 로그아웃 메소드 실행됨 ==========");
    	return "redirect:/"; 
    }
    
    @GetMapping("/clearLoginInterceptorSession")
    @ResponseBody
    public void clearLoginInterceptorSession(HttpSession session) {
        session.removeAttribute("authAlert");
        session.removeAttribute("redirectUrlAfterLogin");
    }
}
