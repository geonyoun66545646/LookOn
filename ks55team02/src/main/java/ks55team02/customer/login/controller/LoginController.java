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

/**
 * 고객 로그인 관련 요청을 처리하는 컨트롤러 클래스입니다.
 * 로그인, 로그아웃, 세션 관리 등의 기능을 제공합니다.
 */
@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
@Slf4j
public class LoginController {

	private final LoginService loginService;

	/**
     * 사용자 로그인 요청을 처리하는 API 엔드포인트입니다.
     * POST 방식으로 로그인 정보를 받아 인증 및 권한 확인 후 결과를 반환합니다.
     *
     * @param loginInfo   요청 본문(RequestBody)으로 전송된 로그인 정보 (아이디, 비밀번호, 리다이렉트 URL 등)
     * @param request     HTTP 요청 객체 (클라이언트 IP 주소 획득에 사용)
     * @param session     HTTP 세션 객체 (로그인 정보 저장 및 중복 로그인 방지에 사용)
     * @return            로그인 처리 결과(성공/실패, 메시지, 리다이렉트 URL 등)를 담은 JSON 응답
     */
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> loginProcess(@RequestBody Login loginInfo, HttpServletRequest request, HttpSession session) {

    	// 이미 로그인된 사용자가 중복 로그인 시도를 하는지 확인합니다.
    	if (session.getAttribute("loginUser") != null) {
            log.warn("이미 로그인된 사용자의 중복 로그인 시도 차단. IP: {}", request.getRemoteAddr());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "already_logged_in");
            response.put("message", "이미 로그인되어 있습니다.");
            // 이미 로그인된 상태이므로 OK(200) 대신 다른 상태 코드(예: 409 Conflict)를 보내는 것도 좋음
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response); 
        }
    	
    	// 클라이언트의 IP 주소를 가져옵니다. X-Real-IP 헤더가 있을 경우 우선적으로 사용합니다.
    	String clientIp = request.getHeader("X-Real-IP");
        // 1. 클라이언트의 IP 주소 가져오기
        clientIp = (clientIp == null) ? request.getRemoteAddr() : clientIp;
        loginInfo.setIpAddress(clientIp);
        log.info("로그인 시도 => ID: {}, IP: {}, 복귀주소: {}", loginInfo.getUserLgnId(), clientIp, loginInfo.getRedirectUrl());
        
        // LoginService를 호출하여 실제 로그인 로직(아이디/비밀번호 확인, 사용자 정보 조회 등)을 실행합니다.
        Login userInfo = loginService.login(loginInfo, request);
        
        Map<String, Object> response = new HashMap<>();
    	
        // 로그인 서비스 처리 결과에 따라 응답을 구성합니다.
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
            	// 계정이 잠겨있는지 확인하고, 잠금 해제 시간이 현재 시간보다 이후인지 검사합니다.
                response.put("status", "locked");
                response.put("message", "계정이 잠겨있습니다. 잠금 해제 시간: " + userInfo.getAcntLockRmvTm());
            } else if (!"활동".equals(userInfo.getUserStatus())) { // '휴면' 또는 '탈퇴' 상태
                // 기존 'inactive' 상태 (이제 '제한'은 위에서 처리되므로 여기는 순수 '휴면', '탈퇴'만 남습니다)
                response.put("status", "inactive");
                response.put("message", "휴면 또는 탈퇴 상태의 계정입니다.");
            } else {
            	// 모든 검사를 통과한 '활동' 상태의 사용자인 경우, 최종 로그인 성공으로 처리합니다.
                response.put("status", "success");
                response.put("message", "로그인에 성공하였습니다.");
                response.put("redirectUrl", loginInfo.getRedirectUrl());
                log.info("세션 생성 완료: {}", loginInfo.getUserLgnId());
            }
        } else {
        	// 로그인 서비스에서 반환된 사용자 정보가 null인 경우 (아이디/비밀번호 불일치 등) 로그인 실패로 처리합니다.
            response.put("status", "fail");
            response.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
        }

    	// 최종 응답을 JSON 형태로 반환합니다. (HTTP 200 OK)
        return ResponseEntity.ok(response);
    }
    
    /**
     * 사용자 로그아웃 요청을 처리하는 메소드입니다.
     * 현재 사용자의 세션을 무효화하고, 메인 페이지로 리다이렉트합니다.
     *
     * @param session 현재 로그인된 사용자의 HTTP 세션 정보
     * @return        로그아웃 후 리다이렉트할 URL (메인 페이지)
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
    	// 세션을 무효화하여 모든 세션 데이터를 삭제하고, 로그인 상태를 해제합니다.
    	session.invalidate();
    	
    	log.info("========== 로그아웃 메소드 실행됨 ==========");
    	return "redirect:/"; 
    }
    
    /**
     * 로그인 인터셉터 세션 속성을 클리어하는 메소드입니다.
     * 주로 로그인 처리 후 불필요해진 세션 속성(예: 인증 경고 메시지, 로그인 후 리다이렉트 URL)을 제거하는 데 사용됩니다.
     *
     * @param session 현재 사용자의 HTTP 세션 정보
     */
    @GetMapping("/clearLoginInterceptorSession")
    @ResponseBody
    public void clearLoginInterceptorSession(HttpSession session) {
        session.removeAttribute("authAlert");
        session.removeAttribute("redirectUrlAfterLogin");
    }
}
