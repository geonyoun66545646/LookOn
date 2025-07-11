package ks55team02.customer.mypage.service.impl;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import ks55team02.customer.login.domain.UserInfoResponse;
import ks55team02.customer.mypage.domain.ProfileResponse;
import ks55team02.customer.mypage.domain.ProfileUpdateRequest;
import ks55team02.customer.mypage.domain.UserUpdateRequest;
import ks55team02.customer.mypage.mapper.MyPageMapper;
import ks55team02.customer.mypage.service.FileStorageService;
import ks55team02.customer.mypage.service.MyPageService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyPageServiceImpl implements MyPageService{

	private final MyPageMapper myPageMapper;
	//private final FileStorageService fileStorageService;

	// [수정1] 회원정보 업데이트 (파일 없음)
    @Override
    @Transactional
    public boolean updateUserCoreInfo(String userNo, UserUpdateRequest request) {
        request.setUserNo(userNo);
        return myPageMapper.updateUserCoreInfo(request) > 0;
    }

    // [수정2] 회원정보 업데이트 (파일 포함)
    @Override
    @Transactional
    public boolean updateUserCoreInfo(String userNo, UserUpdateRequest request, MultipartFile prflImg) {
        
        request.setUserNo(userNo);
//        if (prflImg != null && !prflImg.isEmpty()) {
//            String imagePath = null;
//			try {
//				imagePath = fileStorageService.storeProfileImage(prflImg);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//            request.setPrflImgPath(imagePath); // DTO에 경로 저장
//        }
        return myPageMapper.updateUserCoreInfo(request) > 0;
    }

    // [추가] 프로필 업데이트
    @Override
    @Transactional
    public boolean updateProfile(String userNo, String userNcnm, String selfIntroCn, MultipartFile profileImageFile) {
//    	String imagePath = null;
//        if (profileImageFile != null && !profileImageFile.isEmpty()) {
//            try {
//                imagePath = fileStorageService.storeProfileImage(profileImageFile);
//            } catch (IOException e) {
//                // 혹은 여기서 사용자 정의 예외를 던질 수 있습니다.
//                throw new RuntimeException("프로필 이미지 저장에 실패했습니다.", e);
//            }
//        }
        
        // 2. Mapper에 전달할 DTO 생성 및 데이터 설정
        ProfileUpdateRequest requestDto = new ProfileUpdateRequest();
        requestDto.setUserNo(userNo);
        requestDto.setUserNcnm(userNcnm);
        requestDto.setSelfIntroCn(selfIntroCn);
        //requestDto.setPrflImgPath(imagePath); // 파일이 없으면 null, 있으면 경로가 설정됨

        // 3. Mapper 호출 (Mapper는 int를 반환하므로 boolean으로 변환)
        int updatedRows = myPageMapper.updateProfile(requestDto);
        
        return updatedRows > 0;
    }
    
    @Override
    public ProfileResponse getProfileResponse(String userNo) {
        // Mapper를 호출하여 DB에서 프로필 정보를 조회합니다.
        // Mapper 인터페이스에 findProfileByUserNo 라는 이름의 메소드가 있다고 가정합니다.
        return myPageMapper.findProfileByUserNo(userNo);
    }
    
    @Override
    public UserInfoResponse getUserInfo(String userNo) {
        // Mapper를 호출하여 DB에서 사용자 정보를 조회합니다.
        // 이전에 보내주신 Mapper 코드에 getUserInfo 메소드가 이미 있었습니다.
        return myPageMapper.getUserInfo(userNo);
    
    }
}
