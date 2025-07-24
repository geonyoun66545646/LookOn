package ks55team02.customer.mypage.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.customer.login.domain.UserInfoResponse;
import ks55team02.customer.mypage.domain.UserUpdateRequest;

@Mapper
public interface MyPageUserInfoMapper {

	/**
     * 주어진 사용자 번호(userNo)를 사용하여 특정 사용자의 현재 상세 정보를 조회합니다.
     *
     * @param userNo 조회할 사용자의 고유 번호
     * @return     조회된 사용자 정보를 담은 {@link UserInfoResponse} DTO. 해당하는 사용자가 없을 경우 null을 반환합니다.
     */
    UserInfoResponse findUserInfo(String userNo);

    /**
     * 특정 사용자(userNo)를 제외하고, 주어진 이메일 주소(emlAddr)가 다른 사용자와 중복되는지 여부를 조회합니다.
     * 이는 회원 정보 수정 시 이메일 중복을 방지하기 위해 사용됩니다.
     *
     * @param userNo  현재 정보를 수정하려는 사용자의 고유 번호
     * @param emlAddr 중복 여부를 검사할 이메일 주소
     * @return        주어진 이메일 주소가 현재 사용자를 제외한 다른 사용자에게서 사용되고 있는 경우의 수 (0 또는 1 이상)
     */
    int countByEmailForOthers(@Param("userNo") String userNo, @Param("emlAddr") String emlAddr);

    /**
     * 특정 사용자(userNo)를 제외하고, 주어진 전화번호(telno)가 다른 사용자와 중복되는지 여부를 조회합니다.
     * 이는 회원 정보 수정 시 전화번호 중복을 방지하기 위해 사용됩니다.
     *
     * @param userNo 현재 정보를 수정하려는 사용자의 고유 번호
     * @param telno  중복 여부를 검사할 전화번호
     * @return       주어진 전화번호가 현재 사용자를 제외한 다른 사용자에게서 사용되고 있는 경우의 수 (0 또는 1 이상)
     */
    int countByTelNoForOthers(@Param("userNo") String userNo, @Param("telno") String telno);

    /**
     * 회원의 이름, 이메일, 주소 등 주요 회원 정보를 업데이트합니다.
     *
     * @param request 업데이트할 새로운 회원 정보를 담은 {@link UserUpdateRequest} DTO
     * @return        데이터베이스에서 업데이트된 레코드의 수 (성공 시 1, 실패 시 0)
     */
    int updateUserInfo(UserUpdateRequest request);
}
