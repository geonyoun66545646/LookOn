package ks55team02.customer.login.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Login {

	// ===== users 테이블 정보 =====
    private String userNo;
    private String mbrGrdCd; // 사용자의 등급
    private String userLgnId; // 유저 아이디
    private String userPswdEncptVal; // 입력받는 용도 + DB 값 확인 용도
    private String userNcnm; // 유저 닉네임
    private String userStatus; // 유저 상태
    

    // ===== user_security_settings 테이블 정보 =====
    private int lgnFailNmtm;
    private LocalDateTime acntLockRmvTm; // LocalDateTime 이나 Timestamp 타입이 더 적합할 수 있습니다.

    // ===== 로그인 폼에서만 받는 추가 정보 =====
    // DTO는 DB 테이블과 1:1로만 매핑되는 것이 아니라,
    // 화면과 데이터를 주고받는 목적도 있으므로 이런 필드도 가능합니다.
    private String lgnUseId; // 로그인시 사용된 아이디
    private LocalDateTime lgnDt; // 로그인시 사용된 아이디    
    private String ipAddress; // 컨트롤러에서 채워줄 IP 주소 정보
}
