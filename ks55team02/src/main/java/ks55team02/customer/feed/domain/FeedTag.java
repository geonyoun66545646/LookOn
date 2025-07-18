package ks55team02.customer.feed.domain;

import lombok.Data;

@Data
public class FeedTag {

	// feed tag 테이블
	private String feedTagSn;
	private String feedSn;
	private String tagSn;
	private String crtDt;
	
	private String tagNm; 
	private String creatrUserNo;
}
