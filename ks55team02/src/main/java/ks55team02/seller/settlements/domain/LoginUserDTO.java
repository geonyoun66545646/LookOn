package ks55team02.seller.settlements.domain;

import lombok.Data;

@Data
public class LoginUserDTO {
	private String userId; // 사용자가 로그인에 사용하는 ID
    private String userNo; // users 테이블의 user_no (상점 ID를 찾기 위해 필요)
    private String userName;

}
