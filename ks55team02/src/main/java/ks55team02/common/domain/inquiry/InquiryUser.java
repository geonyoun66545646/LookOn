package ks55team02.common.domain.inquiry;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class InquiryUser {
	 private String userNo; // 사용자 번호 (PK, 애플리케이션 생성 ID)
	    private String mbrGrdCd; // 회원 등급 코드 (FK)
	    private String userLgnId; // 사용자 로그인 아이디
	    private String userNm; // 사용자이름
	    private String userPswdEncptVal; // 사용자 비밀번호 (해싱됨)
	    private String genderSeCd; // 성별 (m/f/o)
	    private String emlAddr; // 이메일 주소
	    private String telno; // 전화 번호
	    private LocalDateTime userBrdt; // 생년월일
	    private String zipCd; // 우편번호
	    private String addr; // 주소
	    private String daddr; // 상세 주소
	    private String userNcnm; // 닉네임
	    private String userStatus; // 회원 상태 (활동, 휴면, 탈퇴 등)
	    private LocalDateTime joinDt; // 가입 일시
	    private LocalDateTime whdwlDt; // 탈퇴 일시
	    private LocalDateTime lastInfoMdfcnDt; // 마지막 정보 수정 일시

}
