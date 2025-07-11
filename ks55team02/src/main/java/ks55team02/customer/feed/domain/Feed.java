package ks55team02.customer.feed.domain;

import java.util.List;

import ks55team02.customer.login.domain.UserInfoResponse;
import lombok.Data;

@Data
public class Feed {
	
	// feed 테이블
	private String feedSn;
	private String wrtrUserNo;
	private String feedCn;
	private String crtDt;
	private String mdfcnDt;
	private String delDt;
	private String delUserNo;
	
	// --- 연관 객체 및 계산 필드 ---
	private UserInfoResponse writerInfo;
	private int likeCount;
    
	private List<FeedImage> imageList;
	private List<FeedComment> commentList;
	private List<FeedTag> tagList;
	
	private FeedImage representativeImage;
	
}
