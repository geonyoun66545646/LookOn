package ks55team02.customer.feed.domain;

import lombok.Data;

@Data
public class FeedImage {

	// feed img 테이블
	private String feedImgSn;
	private String feedSn;
	private String imgFilePathNm;
	private String feedImgSortSn;
	private String imgAltTxtCn;
	private String crtDt;
	private String delDt;
	private String delUserNo;
}
