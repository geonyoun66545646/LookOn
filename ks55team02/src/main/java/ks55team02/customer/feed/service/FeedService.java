package ks55team02.customer.feed.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import ks55team02.customer.feed.domain.Feed;
import ks55team02.customer.feed.domain.FeedComment;
import ks55team02.customer.login.domain.LoginUser;

public interface FeedService {

	Map<String, Object> selectFeedList(String userNo, int page, int size);
	Feed selectFeedDetail(String feedSn);
	List<Feed> selectNextFeedList(String currentFeedCrtDt, int limit, String context, String userNo);
	
	// [수정] 해시태그 파라미터 추가
	void insertFeed(String feedCn, String hashtags, List<MultipartFile> imageFiles, LoginUser loginUser);
	
	// [신규] 피드 수정
	boolean updateFeed(String feedSn, String feedCn, String hashtags, List<String> deleteImageSns, List<MultipartFile> newImageFiles, LoginUser loginUser);
	
	// [신규] 피드 삭제
	boolean deleteFeed(String feedSn, String userNo);

	Map<String, Object> addLike(String feedSn, String userNo);
	FeedComment addComment(String feedSn, String commentText, String userNo);
	boolean deleteComment(String feedCmntSn, String userNo);
	FeedComment updateComment(String feedCmntSn, String commentText, String userNo);
}