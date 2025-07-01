package ks55team02.customer.post.domain;

import java.time.LocalDateTime;

import ks55team02.admin.adminpage.useradmin.userlist.domain.UserList;
import lombok.Data;

@Data
public class Post {
	private String pstSn;
	private String bbsClsfCd;
	private String wrtrUserNo;
	private String pstTtl;
	private String pstCn;
	private String inqCnt;
	private String ntcPstYn;
	private LocalDateTime crtDt;
	private LocalDateTime mdfcnDt;
	private LocalDateTime delDt;
	
	private UserList userInfo;
}
