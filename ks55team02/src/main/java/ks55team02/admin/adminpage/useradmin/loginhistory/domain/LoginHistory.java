package ks55team02.admin.adminpage.useradmin.loginhistory.domain;

import java.time.LocalDateTime;

import ks55team02.admin.common.domain.SearchCriteria;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class LoginHistory extends SearchCriteria {

	private String 			loginHistoryNo;
    private String 			userNo;
    private LocalDateTime 	lgnDt;
    private LocalDateTime 	lgtDt;
    private String 			lgnIpAddr;
    private int 			lgnFailNmtm;
    private String 			userLgnId; // JOIN해서 가져올 회원 ID
}
