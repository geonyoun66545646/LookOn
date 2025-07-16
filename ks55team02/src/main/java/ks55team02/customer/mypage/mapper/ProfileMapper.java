package ks55team02.customer.mypage.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.customer.mypage.domain.ProfileUpdateRequest;

@Mapper
public interface ProfileMapper {

	/**
     * 사용자의 프로필 정보(닉네임, 자기소개, 이미지 경로)를 업데이트합니다.
     * @param request 업데이트할 정보가 담긴 DTO
     * @return 영향을 받은 행의 수 (업데이트 성공 시 1, 실패 시 0)
     */
    int updateProfile(ProfileUpdateRequest request);
    
    int updateUserNickname(@Param("userNo") String userNo, @Param("userNcnm") String userNcnm);
    
    int countByUserNcnmForOthers(@Param("userNo") String userNo, @Param("userNcnm") String userNcnm);
}
