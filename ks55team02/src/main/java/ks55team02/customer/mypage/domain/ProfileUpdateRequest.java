package ks55team02.customer.mypage.domain;


import lombok.Data;

@Data
public class ProfileUpdateRequest {

	private String userNo;         // 세션에서 주입
    private String userNcnm;       // 닉네임
    private String selfIntroCn;    // 자기소개
    private String prflImgPath; // 추가된 필드 ★
}
