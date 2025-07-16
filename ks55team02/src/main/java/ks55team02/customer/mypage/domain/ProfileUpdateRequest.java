package ks55team02.customer.mypage.domain;

import lombok.Data;

@Data
public class ProfileUpdateRequest {

	 /**
     * 업데이트할 사용자의 고유 번호 (Service에서 주입)
     */
    private String userNo;

    /**
     * 새로 변경할 사용자의 닉네임
     */
    private String userNcnm;

    /**
     * 새로 변경할 자기소개 내용
     */
    private String selfIntroCn;

    /**
     * 서버에 저장된 후, DB에 저장될 이미지의 최종 경로 (Service에서 설정)
     * 예: /attachment/profiles/2025/07/16/xxxxxxxx-xxxx.jpg
     */
    private String prflImgPath;
}
