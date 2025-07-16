package ks55team02.admin.login.domain;

import lombok.Data;

@Data
public class AdminLoginRequest {

	private String adminId; // 로그인 폼의 name="adminId"
    private String adminPw; // 로그인 폼의 name="adminPw"
}
