package ks55team02.customer.login.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.UserSanction;
import ks55team02.admin.adminpage.inquiryadmin.reports.mapper.AdminReportsMapper;
import ks55team02.customer.login.domain.Login;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.customer.login.mapper.LoginMapper;
import ks55team02.customer.login.service.LoginService;
import ks55team02.systems.crypto.utils.PasswordEncryptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl implements LoginService {

	private final LoginMapper loginMapper;
	private final AdminReportsMapper adminReportsMapper; // 25-07-22 염가은 추가
	
	/**
	 * 25-07-22 염가은 추가
	 * [추가] 특정 사용자의 가장 최신 제재 기록을 조회하는 메소드 구현
	 */
	@Override
	public UserSanction getLatestSanctionByUserNo(String userNo) {
		return adminReportsMapper.getLatestSanctionByUserNo(userNo);
	}

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
			
			// 25-07-22 염가은 추가
			// [추가 시작 지점]
	        // 25-07-22 염가은 추가: '제한' 상태인 경우, 임시로 userNo를 세션에 저장하여 SanctionInfoController에서 활용
	        if ("제한".equals(userStatus)) {
	            // 제재된 사용자의 userNo를 세션에 임시로 저장합니다.
	            // 이 세션은 SanctionInfoController에서 제재 상세 정보를 조회할 때 사용됩니다.
	            request.getSession().setAttribute("tempSanctionUserNo", userInfoFromDb.getUserNo());
	            
	            // 아래는 기존에 있었던 제재 상세 정보 로그 출력 로직입니다.
	            UserSanction latestSanction = getLatestSanctionByUserNo(userInfoFromDb.getUserNo()); 
	            if (latestSanction != null) {
	                log.info("제한된 계정 로그인 시도 상세: 제재 타입 - {}, 사유 - {}, 종료일 - {}",
	                        latestSanction.getSanctionType(), latestSanction.getSanctionReason(), latestSanction.getSanctionEndDt());
	            }
	        }
	        // [추가 끝 지점]
			
			return userInfoFromDb; // 상태 정보를 전달하기 위해 객체 반환
		}

		// 4. 비밀번호 비교
		String inputPassword = loginInfo.getUserPswdEncptVal();
		String dbPassword = userInfoFromDb.getUserPswdEncptVal();

		boolean isPasswordMatch = PasswordEncryptor.checkPassword(inputPassword, dbPassword);

		// 4-1. 비밀번호 불일치 (로그인 실패 처리)
		if (!isPasswordMatch) {
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

		String gradeCode = userInfoFromDb.getMbrGrdCd(); // 등급 코드를 가져옵니다.

		// 등급 코드가 'grd_cd_0'(총관리자) 또는 'grd_cd_1'(관리자)인지 확인합니다.
		if ("grd_cd_0".equals(gradeCode) || "grd_cd_1".equals(gradeCode)) {
			// 서버 로그에는 관리자 로그인 시도임을 명확히 기록합니다.
			log.warn("관리자 계정으로 메인 페이지 로그인 시도 차단. ID: {}, 등급: {}", userInfoFromDb.getUserLgnId(), gradeCode);

			// 컨트롤러에는 일반적인 로그인 실패와 동일하게 null을 반환합니다.
			return null;
		}

		// 성공 시, 실패 횟수 초기화 (실패 기록이 있을 경우에만)
		if (userInfoFromDb.getLgnFailNmtm() > 0) {
			loginMapper.resetLoginFailCount(userId);
		}

		// 5. 로그인 성공 처리
		userInfoFromDb.setLgnFailNmtm(0); // 성공 플래그(0)
		userInfoFromDb.setIpAddress(loginInfo.getIpAddress());
		loginMapper.addLoginHistory(userInfoFromDb);

		// 6. 컨트롤러에 최종 사용자 정보 반환
		LoginUser sessionUser = new LoginUser(userInfoFromDb.getUserNo(), userInfoFromDb.getMbrGrdCd(),
				userInfoFromDb.getUserLgnId(), userInfoFromDb.getUserNcnm());

		// 1. 현재 사용 중인 세션을 가져온다. (없으면 새로 생성하지 않음)
		HttpSession oldSession = request.getSession(false);
		if (oldSession != null) {
			// 2. 기존 세션이 있다면, 그 세션을 무효화(파기)한다.
			log.info("기존 세션 무효화. Session ID: {}", oldSession.getId());
			oldSession.invalidate();
		}

		// 3. 완전히 새로운 세션을 생성한다. (true: 없으면 반드시 새로 생성)
		HttpSession newSession = request.getSession(true);

		// 4. 새로운 세션에 로그인 사용자 정보를 저장한다.
		newSession.setAttribute("loginUser", sessionUser);
		log.info("새로운 세션 생성 및 사용자 정보 저장 완료. New Session ID: {}", newSession.getId());

		return userInfoFromDb;
	}
}
