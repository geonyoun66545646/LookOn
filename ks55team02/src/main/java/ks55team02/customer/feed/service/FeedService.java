package ks55team02.customer.feed.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import ks55team02.customer.feed.domain.Feed;
import ks55team02.customer.feed.domain.FeedComment;
import ks55team02.customer.login.domain.LoginUser;

public interface FeedService {

	// 피드 목록 조회
	Map<String, Object> selectFeedList(String userNo, int page, int size);
	
	// 피드 상세 조회 
	Feed selectFeedDetail(String feedSn);
	
	// 피드 다음 페이지 조회
	List<Feed> selectNextFeedList(String currentFeedCrtDt, int limit, String context, String userNo);
	
	// 피드 등록
	void insertFeed(String feedCn, List<MultipartFile> imageFiles, LoginUser loginUser);
	
	// 좋아요 추가
	Map<String, Object> addLike(String feedSn, String userNo);

	// [신규] 댓글 추가
	FeedComment addComment(String feedSn, String commentText, String userNo);
}