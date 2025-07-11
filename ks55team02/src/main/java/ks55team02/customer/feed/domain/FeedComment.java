package ks55team02.customer.feed.domain;

import lombok.Data;

@Data
public class FeedComment {

	// feed comment 테이블
	private String feedCmntSn;
	private String feedSn;
	private String wrtrUserNo;
	private String prntFeedCmntSn;
	private String cmntCn;
	private String crtDt;
	private String mdfcnDt;
	private String delDt;
	private String delUserNo;
}
