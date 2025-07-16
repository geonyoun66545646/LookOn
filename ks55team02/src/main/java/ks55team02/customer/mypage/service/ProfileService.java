package ks55team02.customer.mypage.service;

import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

	 /**
     * 사용자의 프로필 정보(닉네임, 자기소개, 이미지)를 업데이트합니다.
     *
     * @param userNo 업데이트할 사용자의 고유 번호
     * @param userNcnm 새로 변경할 닉네임
     * @param selfIntroCn 새로 변경할 자기소개
     * @param profileImage 업로드된 프로필 이미지 파일
     */
    void updateProfile(String userNo, String userNcnm, String selfIntroCn, MultipartFile profileImage);
}
