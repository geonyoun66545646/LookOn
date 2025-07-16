package ks55team02.admin.login.domain;

import lombok.Data;

@Data
public class AdminInfo {

	private String userNo;
    private String userLgnId;
    private String userPswd; // 암호화된 비밀번호를 담을 필드
    private String userNm;
    private String mbrGrdCd;
}
