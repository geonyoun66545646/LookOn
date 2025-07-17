package ks55team02.customer.mypage.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.customer.login.domain.UserInfoResponse;
import ks55team02.customer.mypage.domain.UserUpdateRequest;

@Mapper
public interface MyPageUserInfoMapper {

	/**
     * 특정 사용자의 현재 정보를 조회
     * @param userNo 사용자 번호
     * @return 사용자 정보 DTO
     */
    UserInfoResponse findUserInfo(String userNo);

    /**
     * 특정 사용자를 제외하고, 이메일 또는 연락처가 일치하는 다른 사용자의 수를 조회
     * @param params 맵(emlAddr, telno, userNo)
     * @return 중복되는 사용자 수
     */
    int countByEmailForOthers(@Param("userNo") String userNo, @Param("emlAddr") String emlAddr);
    int countByTelNoForOthers(@Param("userNo") String userNo, @Param("telno") String telno);

    /**
     * 회원정보(이름, 이메일, 주소 등)를 업데이트
     * @param request 업데이트 요청 DTO
     * @return 영향을 받은 행의 수 (0 또는 1)
     */
    int updateUserInfo(UserUpdateRequest request);
}
