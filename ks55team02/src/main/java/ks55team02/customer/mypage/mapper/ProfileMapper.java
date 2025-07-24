package ks55team02.customer.mypage.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.customer.mypage.domain.ProfileUpdateRequest;

@Mapper
public interface ProfileMapper {

	/**
     * 사용자의 프로필 정보(닉네임, 자기소개, 이미지 경로 등)를 업데이트합니다.
     * 이 메소드는 {@link ProfileUpdateRequest} DTO에 담긴 모든 프로필 관련 정보를 업데이트할 때 사용될 수 있습니다.
     *
     * @param request 업데이트할 프로필 정보를 담은 {@link ProfileUpdateRequest} DTO
     * @return        데이터베이스에서 업데이트된 레코드의 수 (성공 시 1, 실패 시 0)
     */
    int updateProfile(ProfileUpdateRequest request);
    
    /**
     * 특정 사용자의 닉네임만 업데이트합니다.
     * 이 메소드는 프로필 이미지나 자기소개는 변경하지 않고 닉네임만 변경할 때 유용합니다.
     *
     * @param userNo   닉네임을 변경할 사용자의 고유 번호
     * @param userNcnm 새로 설정할 닉네임
     * @return         데이터베이스에서 업데이트된 레코드의 수 (성공 시 1, 실패 시 0)
     */
    int updateUserNickname(@Param("userNo") String userNo, @Param("userNcnm") String userNcnm);
    
    /**
     * 특정 사용자(userNo)를 제외하고, 주어진 닉네임(userNcnm)이 다른 사용자와 중복되는지 여부를 조회합니다.
     * 이는 닉네임 변경 시 중복을 방지하기 위해 사용됩니다.
     *
     * @param userNo   현재 닉네임을 변경하려는 사용자의 고유 번호
     * @param userNcnm 중복 여부를 검사할 닉네임
     * @return         주어진 닉네임이 현재 사용자를 제외한 다른 사용자에게서 사용되고 있는 경우의 수 (0 또는 1 이상)
     */
    int countByUserNcnmForOthers(@Param("userNo") String userNo, @Param("userNcnm") String userNcnm);
}
