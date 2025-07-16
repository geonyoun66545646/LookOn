package ks55team02.customer.login.service;

import ks55team02.customer.login.domain.UserInfoResponse;

public interface UserInfoService {

	/**
     * 사용자 번호(userNo)로 사용자의 상세 정보를 조회하는 메소드
     * @param userNo 조회할 사용자의 고유 번호
     * @return users, users_profiles 등 관련 테이블 정보가 모두 담긴 DTO
     */
    UserInfoResponse getUserInfo(String userNo);

}
