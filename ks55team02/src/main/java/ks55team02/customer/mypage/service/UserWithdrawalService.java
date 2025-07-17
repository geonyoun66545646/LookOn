package ks55team02.customer.mypage.service;

import ks55team02.customer.login.domain.LoginUser;

public interface UserWithdrawalService {

	/**
     * 사용자 회원 탈퇴를 처리합니다.
     * @param loginUser 현재 로그인된 사용자 정보
     * @param rawPassword 사용자가 본인 확인을 위해 입력한 평문 비밀번호
     * @throws IllegalArgumentException 비밀번호가 일치하지 않을 경우 발생
     */
    void withdrawUser(LoginUser loginUser, String rawPassword);
}
