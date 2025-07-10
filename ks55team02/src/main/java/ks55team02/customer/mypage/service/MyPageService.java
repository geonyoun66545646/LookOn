package ks55team02.customer.mypage.service;

import org.springframework.web.multipart.MultipartFile;

import ks55team02.customer.login.domain.UserInfoResponse;
import ks55team02.customer.mypage.domain.ProfileResponse;
import ks55team02.customer.mypage.domain.ProfileUpdateRequest;
import ks55team02.customer.mypage.domain.UserUpdateRequest;

public interface MyPageService {

	boolean updateUserCoreInfo(String userNo, UserUpdateRequest request, MultipartFile prflImg);
    UserInfoResponse getUserInfo(String userNo);
    
    // 새 메서드 추가 (파일 없는 경우)
    boolean updateUserCoreInfo(String userNo, UserUpdateRequest request);

    // 프로필 업데이트 메서드
    boolean updateProfile(String userNo, String userNcnm, String selfIntroCn, MultipartFile profileImageFile);
	ProfileResponse getProfileResponse(String userNo);
	
}
