package ks55team02.customer.register.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserSecurity {

	private String 			userNo;
    private LocalDateTime 	lastPswdChgDt;
    private int 			lgnFailNmtm;
    private LocalDateTime 	acntLockRmvTm;
    private Boolean 		mfaUseYn;
    private String 			mfaScrtKeyEncptVal;
    private LocalDateTime 	mfaLastCertScsDt;
    private String 			mfaRcyrCdHashListVal;
    private LocalDateTime 	lastScrtStngMdfcnDt;
}

