package ks55team02.customer.feed.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ks55team02.customer.feed.domain.Feed;

@Service
public interface FeedService {
	// 피드 목록 조회
	Map<String, Object> selectFeedList(String userNo, int page, int size);
	
	// 피드 상세 조회 
	Feed selectFeedDetail(String feedSn);
	
	// 피드 다음 페이지 조회(신규)
	List<Feed> selectNextFeedList(String currentFeedCrtDt, int limit, String context, String userNo);
	
}
