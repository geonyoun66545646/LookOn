package ks55team02.customer.login.mapper;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.customer.login.domain.UserInfoResponse;

@Mapper
public interface UserInfoMapper {

	/**
	 * 주어진 사용자 번호(userNo)를 기반으로 사용자의 상세 정보를 조회합니다.
	 * 조회된 정보는 {@link UserInfoResponse} 객체에 매핑됩니다.
	 *
	 * @param userNo 조회하고자 하는 사용자의 고유 번호 (회원 번호)
	 * @return     조회된 사용자 정보를 담은 {@link UserInfoResponse} 객체.
	 * 해당 userNo에 해당하는 사용자가 없을 경우 null을 반환합니다.
	 */
	UserInfoResponse findUserInfoByUserNo(String userNo);
}
