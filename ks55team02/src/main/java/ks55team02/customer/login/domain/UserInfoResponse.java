package ks55team02.customer.login.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserInfoResponse {

	// ===== users 테이블 정보 =====
    private String userNo;					// 사용자의 번호
    private String mbrGrdCd; 				// 사용자의 등급
    private String userLgnId; 				// 유저 아이디
    private String userNm; 					// 유저 아이디
    private String genderSeCd; 				// 입력받는 용도 + DB 값 확인 용도
    private String emlAddr; 				// 입력받는 용도 + DB 값 확인 용도
    private String telno; 					// 입력받는 용도 + DB 값 확인 용도
    private String userBrdt; 				// 입력받는 용도 + DB 값 확인 용도
    private String zipCd; 					// 입력받는 용도 + DB 값 확인 용도
    private String addr; 					// 입력받는 용도 + DB 값 확인 용도
    private String daddr; 					// 입력받는 용도 + DB 값 확인 용도
    private String userNcnm; 				// 유저 닉네임
    private String userStatus; 				// 유저 상태
    
    // ===== users_profiles 테이블 정보 =====
    private String prflImg; 				// 프로필 이미지
    private String selfIntroCn; 			// 사용자 자기소개
    private Boolean pushNtfctnRcptnAgreYn; 	// 푸시 알림 수신 동의
    private Boolean emlRcptnAgreYn; 		// 이메일 수신동의
    private LocalDateTime lastPrflMdfcnDt; 	// 마지막 프로필 수정 일시
    

    // ===== user_security_settings 테이블 정보 =====
    private String lastPswdChgDt; 			// 마지막 비밀번호 변경 일시
    private Boolean mfaUseYn;				// 2단계인증 사용 여부
    
    // ===== user_follows 테이블 정보, 추가로 게시글 갯수까지 =====
    private int postCount;
    private int followerCount;
    private int followingCount;
}
