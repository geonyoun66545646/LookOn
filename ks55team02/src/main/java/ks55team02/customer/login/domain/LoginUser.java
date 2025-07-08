package ks55team02.customer.login.domain;



import java.io.Serializable;


import lombok.Getter;

@Getter
public class LoginUser  implements Serializable{
	
	private static final long serialVersionUID = 1L;

	// ===== users 테이블 정보 =====
    private String userNo;					// 사용자의 번호
    private String mbrGrdCd; 				// 사용자의 등급
    private String userLgnId; 				// 유저 아이디
    private String userNcnm; 				// 유저 닉네임
    
    public LoginUser(String userNo, String mbrGrdCd, String userLgnId, String userNcnm) {
    	this.userNo = userNo;
    	this.mbrGrdCd = mbrGrdCd;
    	this.userLgnId = userLgnId;
    	this.userNcnm = userNcnm;
    }
}
