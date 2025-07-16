package ks55team02.customer.mypage.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import ks55team02.customer.mypage.domain.ProfileUpdateRequest;
import ks55team02.customer.mypage.mapper.ProfileMapper;
import ks55team02.customer.mypage.service.ProfileService;
import ks55team02.util.FileDetail;
import ks55team02.util.FilesUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService{

	private final ProfileMapper profileMapper;
    private final FilesUtils filesUtils; // 팀의 규칙인 FilesUtils를 주입받습니다.

    @Override
    @Transactional
    public void updateProfile(String userNo, String userNcnm, String selfIntroCn, MultipartFile profileImage) {

        String dbImagePath = null;

        // 1. 프로필 이미지 파일이 존재하고, 내용이 비어있지 않다면
        if (profileImage != null && !profileImage.isEmpty()) {
            FileDetail fileDetail = filesUtils.saveFile(profileImage, "profiles");
            dbImagePath = fileDetail.getSavedPath();
        }
        
        // --- ▼▼▼ 로직 변경 부분 ▼▼▼ ---

        // 2. [users 테이블] 닉네임 업데이트
        //    닉네임 값이 존재할 경우에만 Mapper를 호출합니다.
        if (userNcnm != null) {
        	int duplicateCount = profileMapper.countByUserNcnmForOthers(userNo, userNcnm);
            if (duplicateCount > 0) {
                // 중복된 닉네임이 있으면 예외를 발생시켜 컨트롤러가 처리하도록 함
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            profileMapper.updateUserNickname(userNo, userNcnm);
        }
        
        // 3. [user_profiles 테이블] 자기소개 및 이미지 경로 업데이트
        //    업데이트할 내용이 하나라도 있을 경우에만 Mapper를 호출합니다.
        if (selfIntroCn != null || dbImagePath != null) {
            ProfileUpdateRequest requestDto = new ProfileUpdateRequest();
            requestDto.setUserNo(userNo);
            requestDto.setSelfIntroCn(selfIntroCn);
            requestDto.setPrflImgPath(dbImagePath);
            
            profileMapper.updateProfile(requestDto);
        }
    }
}
