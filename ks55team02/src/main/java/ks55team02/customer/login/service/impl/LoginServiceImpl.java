package ks55team02.customer.login.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import ks55team02.customer.login.domain.Login;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.customer.login.mapper.LoginMapper;
import ks55team02.customer.login.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl  implements LoginService{

	private final LoginMapper loginMapper;
	
		@Override
		public Login login(Login loginInfo, HttpServletRequest request) {
		// 1. 사용자 아이디로 회원 정보 조회 (Mapper 호출)
        String userId = loginInfo.getUserLgnId();
        Login userInfoFromDb = loginMapper.getLoginUserInfo(userId);
        
        log.info("조회된 회원 정보: {}", userInfoFromDb);

        // 2. 조회된 회원 정보가 있는지 확인
        if (userInfoFromDb == null) {
            log.info("해당 아이디의 회원이 존재하지 않습니다. ID: {}", userId);
            return null; // 실패
        }

        // 3. 계정 상태 확인 
        // 3-1. 계정 상태 확인 (계정 잠금, 휴면 등)
        LocalDateTime lockTime = userInfoFromDb.getAcntLockRmvTm();
        if (lockTime != null && lockTime.isAfter(LocalDateTime.now())) {
            log.warn("잠긴 계정으로 로그인 시도. ID: {}, 잠금 해제 시간: {}", userId, lockTime);
            // 프론트에서 이 정보를 활용하여 "계정이 잠겼습니다" 메시지 표시 가능
            return userInfoFromDb; // 잠금 정보를 전달하기 위해 객체 반환
        }

        // 3-2. 계정 활성 상태 확인 ('활동'이 아닌 경우)
        String userStatus = userInfoFromDb.getUserStatus();
        if (!"활동".equals(userStatus)) {
            log.warn("비활성 계정으로 로그인 시도. ID: {}, 상태: {}", userId, userStatus);
            return userInfoFromDb; // 상태 정보를 전달하기 위해 객체 반환
        }

        // 4. 비밀번호 비교
        String inputPassword = loginInfo.getUserPswdEncptVal();
        String dbPassword = userInfoFromDb.getUserPswdEncptVal();
        
        // 4-1. 비밀번호 불일치 (로그인 실패 처리)
        if (!inputPassword.equals(dbPassword)) {
            log.info("비밀번호가 일치하지 않습니다. ID: {}", userId);
            
            // 1. 로그인 실패 횟수 1 증가
            loginMapper.incrementLoginFailCount(userId);
            
            // 2. 실패 로그 기록을 위해 정보 설정
            userInfoFromDb.setLgnFailNmtm(1); // 0:성공, 1:실패 플래그
            userInfoFromDb.setIpAddress(loginInfo.getIpAddress());
            loginMapper.addLoginHistory(userInfoFromDb);
            
         	// 3. 계정 잠금 확인 (실패 횟수가 5가 도달 시)
            Login updatedUserInfo = loginMapper.getLoginUserInfo(userId);
            if (updatedUserInfo.getLgnFailNmtm() >= 5) {
                log.info("로그인 5회 이상 실패. 계정이 15분동안 잠김상태가 됩니다. ID: {}", userId);
                loginMapper.lockUserAccount(userId);
            }
            return null; // 실패
        }
        
        // 4-2. 비밀번호 일치 (로그인 성공 처리)
        log.info("로그인 성공! ID: {}", userId);
        
    	// 성공 시, 실패 횟수 초기화 (실패 기록이 있을 경우에만)
        if (userInfoFromDb.getLgnFailNmtm() > 0) {
            loginMapper.resetLoginFailCount(userId);
        }

        // 5. 로그인 성공 처리
        userInfoFromDb.setLgnFailNmtm(0); // 성공 플래그(0)
        userInfoFromDb.setIpAddress(loginInfo.getIpAddress());
        loginMapper.addLoginHistory(userInfoFromDb);

        // 6. 컨트롤러에 최종 사용자 정보 반환
        LoginUser sessionUser = new LoginUser(
                userInfoFromDb.getUserNo(),
                userInfoFromDb.getMbrGrdCd(),
                userInfoFromDb.getUserLgnId(),
                userInfoFromDb.getUserNcnm()
            );
        
        
        HttpSession session = request.getSession(); // 파라미터로 받은 request 사용
        session.setAttribute("loginUser", sessionUser);
        log.info("세션에 사용자 정보 저장 완료. sessionUser: {}", sessionUser);
        
        return userInfoFromDb;
    }
}
