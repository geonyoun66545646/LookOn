package ks55team02.customer.post.domain;

import java.time.LocalDateTime;

import ks55team02.customer.login.domain.UserInfoResponse;
import lombok.Data;

@Data
public class PostComment {
	private String pstCmntSn;
	private String pstSn;
	private String wrtrUserNo;
	private String prntCmntSn;
	private String cmntCn;
	private LocalDateTime crtDt;
	private LocalDateTime mdfcnDt;
	private LocalDateTime delDt;
	private String delUserNo;
	// [수정] 댓글 작성자 정보를 UserInfoResponse 객체로 통합
	private UserInfoResponse writerInfo;
}
