package ks55team02.customer.feed.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import ks55team02.customer.feed.domain.Feed;

@Service
public interface FeedService {
	// 피드 목록 조회
	Map<String, Object> selectFeedList(int page, int pageSize);
	
	// 피드 상세 조회 
	Feed selectFeedDetail(String feedSn);
	
	// 피드 다음 페이지 조회
	Feed selectNextFeed(String currentFeedCrtDt, String wrtrUserNo);
	
	// 마이 피드 목록 조회
	Map<String, Object> selectFeedListByMe(String userNo, int page, int size);
	
}
