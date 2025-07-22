package ks55team02.customer.login.service;

import jakarta.servlet.http.HttpServletRequest;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.UserSanction; // 25-07-22 염가은 추가
import ks55team02.customer.login.domain.Login;

public interface LoginService {

	/**
	 * 사용자의 로그인 요청을 처리하는 메소드
	 * 
	 * @param loginInfo (사용자가 입력한 아이디와 비밀번호가 담긴 DTO)
	 * @return 로그인 성공 시 사용자 정보가 담긴 DTO, 실패 시 null 또는 예외 발생
	 */
	Login login(Login loginInfo, HttpServletRequest request);

	/**
	 * 25-07-22 염가은 추가
	 * [추가] 특정 사용자의 가장 최신 제재 기록을 조회하는 메소드 이 메소드는 사용자가 제재 안내 페이지에 접근할 때, 어떤 제재를 받았는지
	 * 보여주기 위해 사용됩니다.
	 * 
	 * @param userNo 제재 기록을 조회할 사용자 번호 (예: 'user001')
	 * @return 가장 최신 UserSanction 객체 또는 기록이 없을 경우 null
	 */
	UserSanction getLatestSanctionByUserNo(String userNo); // [추가]
}
