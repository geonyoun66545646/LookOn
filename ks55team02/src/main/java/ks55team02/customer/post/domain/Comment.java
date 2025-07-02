package ks55team02.customer.post.domain;

import java.time.LocalDateTime;

import ks55team02.admin.adminpage.useradmin.userlist.domain.UserList;
import lombok.Data;

@Data
public class Comment {
	private String pstCmntSn;
	private String pstSn;
	private String wrtrUserNo;
	private String prntCmntSn;
	private String cmntCn;
	private LocalDateTime crtDt;
	private LocalDateTime mdfcnDt;
	private LocalDateTime delDt;
	private String delUserNo;
	
	private UserList userInfo;
	private String userNcnm;
}
