package ks55team02.customer.mypage.domain;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class UserUpdateRequest {

	private String userNo; // 서비스에서 주입
    private String userNm;
    private String emlAddr;
    private String telno;
    private String zipCd;
    private String addr;
    private String daddr;
    private String userNcnm;      // 닉네임
    private String selfIntroCn;   // 자기소개
    private String prflImgPath;
}
