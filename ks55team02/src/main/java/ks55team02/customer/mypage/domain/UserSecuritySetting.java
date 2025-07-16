package ks55team02.customer.mypage.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserSecuritySetting {

	private String userNo; // 사용자 번호 
    private LocalDateTime lastPswdChgDt; // 마지막 비밀번호 변경일시  
    private LocalDateTime lastStrgMdfcnDt; // 마지막 보안 설정 수정일시 
}