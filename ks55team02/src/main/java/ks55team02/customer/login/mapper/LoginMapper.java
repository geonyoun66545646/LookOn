package ks55team02.customer.login.mapper;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.customer.login.domain.Login;

@Mapper
public interface LoginMapper {

	/**
	 * 주어진 사용자 아이디(userLgnId)로 회원 정보를 조회합니다.
	 *
	 * @param userLgnId 조회할 사용자의 로그인 아이디
	 * @return          조회된 사용자 정보를 담은 {@link Login} 객체. 해당 아이디의 사용자가 없을 경우 null을 반환합니다.
	 */
	Login getLoginUserInfo(String userLgnId);
	
	/**
	 * 특정 사용자의 로그인 실패 횟수를 1 증가시킵니다.
	 *
	 * @param userId 로그인 실패가 발생한 사용자의 아이디
	 */
	void incrementLoginFailCount(String userId);
	
	/**
	 * 특정 사용자의 계정을 잠금 처리하고, 잠금 해제 시간을 설정합니다.
	 *
	 * @param userId 계정을 잠글 사용자의 아이디
	 */
	void lockUserAccount(String userId);
	
	/**
	 * 사용자의 로그인 시도 기록을 데이터베이스에 추가합니다.
	 *
	 * @param loginHistoryInfo 기록할 로그인 시도 정보를 담은 {@link Login} DTO (사용자 ID, IP 주소 등)
	 */
	void addLoginHistory(Login loginHistoryInfo);
	
	/**
     * 로그인 성공 시, 해당 사용자의 로그인 실패 횟수를 0으로 초기화합니다.
     *
     * @param userId 로그인에 성공한 사용자의 아이디
     */
    void resetLoginFailCount(String userId);
}
