package ks55team02.customer.feed.domain;

import lombok.Data;

@Data
public class FeedInteraction {

	// feed inter 테이블
	private String feedIntractSn;
	private String feedSn;
	private String userNo;
	private String intractTypeCd;
	private String crtDt;
	private String rtrcnDt;
}
