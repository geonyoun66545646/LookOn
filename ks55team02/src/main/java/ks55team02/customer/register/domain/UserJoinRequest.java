package ks55team02.customer.register.domain;

import lombok.Data;

@Data
public class UserJoinRequest {

// === 사용자가 회원가입 폼에 직접 입력하는 필드 (최종) ===
    
    // 1. 아이디
    private String userLgnId;

    // 2. 비밀번호
    private String userPswd;
    private String userPswdConfirm; // 비밀번호 확인

    // 3. 이름
    private String userNm;

    // 4. 닉네임
    private String userNcnm;

    // 5. 이메일
    private String emlAddr;
    
    // 6. 전화번호 (3부분으로 나눠서 받을 경우)
    private String telno1;
    private String telno2;
    private String telno3;

    // 7. 주소
    private String zipCd;
    private String addr;
    private String daddr;
    
    // 8. 성별
    private String genderSeCd;
    
    // 9. 선택적 동의 항목 (폼에 체크박스가 있다면 포함)
    private Boolean pushNtfctnRcptnAgreYn;
    private Boolean emlRcptnAgreYn;
}
