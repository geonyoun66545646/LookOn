package ks55team02.customer.mypage.service;

import ks55team02.customer.mypage.domain.UserUpdateRequest;

public interface MyPageUserInfoService {

	/**
     * 회원정보 수정을 처리합니다. (중복 검사 및 변경 감지 로직 포함)
     * @param userNo 현재 로그인한 사용자의 번호
     * @param requestData 수정할 정보가 담긴 DTO
     * @return 처리 결과 메시지 (String)
     * @throws IllegalArgumentException 이메일 또는 연락처가 중복될 경우 발생
     */
    String updateUserInfo(String userNo, UserUpdateRequest requestData);
}
