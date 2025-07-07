package ks55team02.customer.login.mapper;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.customer.login.domain.Login;

@Mapper
public interface LoginMapper {

	// 사용자 아이디로 회원 정보 조회
	Login getLoginUserInfo(String userLgnId);
	
	/**
	 * 로그인 실패 횟수를 1 증가시키는 메소드
	 * @param userId 실패한 사용자의 아이디
	 */
	void incrementLoginFailCount(String userId);
	
	/**
	 * 계정을 잠그는 메소드 (잠금 해제 시간 설정)
	 * @param userId 잠글 사용자의 아이디
	 */
	void lockUserAccount(String userId);
	
	/**
	 * 로그인 기록을 추가하는 메소드
	 * @param loginHistoryInfo 기록할 정보가 담긴 Login DTO
	 */
	void addLoginHistory(Login loginHistoryInfo);
	
	/**
     * 로그인 성공 시, 실패 횟수를 0으로 초기화하는 메소드
     * @param userId 성공한 사용자의 아이디
     */
    void resetLoginFailCount(String userId);
}
