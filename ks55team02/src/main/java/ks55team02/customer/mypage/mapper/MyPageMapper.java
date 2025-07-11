package ks55team02.customer.mypage.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.customer.login.domain.UserInfoResponse;
import ks55team02.customer.mypage.domain.ProfileResponse;
import ks55team02.customer.mypage.domain.ProfileUpdateRequest;
import ks55team02.customer.mypage.domain.UserUpdateRequest;

@Mapper
public interface MyPageMapper {
	
	int updateUserCoreInfo(UserUpdateRequest request);
    UserInfoResponse getUserInfo(String userNo); // 기존 DTO 재사용
    
    int updateProfile(ProfileUpdateRequest request);
    ProfileResponse findProfileByUserNo(String userNo);
}
