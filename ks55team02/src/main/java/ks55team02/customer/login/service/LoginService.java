package ks55team02.customer.login.service;

import jakarta.servlet.http.HttpServletRequest;
import ks55team02.customer.login.domain.Login;

public interface LoginService {

	/**
     * 사용자의 로그인 요청을 처리하는 메소드
     * @param loginInfo (사용자가 입력한 아이디와 비밀번호가 담긴 DTO)
     * @return 로그인 성공 시 사용자 정보가 담긴 DTO, 실패 시 null 또는 예외 발생
     */
	Login login(Login loginInfo, HttpServletRequest request);
}
