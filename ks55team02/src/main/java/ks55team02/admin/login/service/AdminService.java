package ks55team02.admin.login.service;

import ks55team02.admin.login.domain.AdminLoginRequest;
import ks55team02.customer.login.domain.LoginUser;

public interface AdminService {

	/**
     * 관리자 로그인을 처리하고, 성공 시 세션에 저장할 LoginUser 객체를 반환합니다.
     * @param loginRequest 관리자 아이디와 비밀번호가 담긴 DTO
     * @return 로그인 성공 시 LoginUser 객체, 실패 시 null
     */
    LoginUser login(AdminLoginRequest loginRequest);
}
