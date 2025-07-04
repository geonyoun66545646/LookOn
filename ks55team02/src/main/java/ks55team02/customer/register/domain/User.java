package ks55team02.customer.register.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class User {

	    private String 			userNo;
	    private String 			mbrGrdCd; // 회원 가입시 기본값: grd_cd_3
	    private String 			userLgnId;
	    private String 			userNm;
	    private String 			userPswdEncptVal; // DB에 저장될 비밀번호 필드
	    private String 			genderSeCd;
	    private String 			emlAddr;
	    private String 			telno;
	    private LocalDate 		userBrdt;
	    private String 			zipCd;
	    private String 			addr;
	    private String 			daddr;
	    private String			userNcnm;
	    private String			userStatus; 		// 회원 가입시 기본값: 활동
	    private LocalDateTime 	joinDt;
	    private LocalDateTime 	whdwlDt;
	    private LocalDateTime 	lastInfoMdfcnDt;
}
