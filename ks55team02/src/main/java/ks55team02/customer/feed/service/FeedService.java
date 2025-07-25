package ks55team02.customer.feed.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import ks55team02.customer.feed.domain.Feed;
import ks55team02.customer.feed.domain.FeedComment;
import ks55team02.customer.login.domain.LoginUser;

public interface FeedService {

	Map<String, Object> selectFeedList(String userNo, int page, int size, String sortOrder);
	Feed selectFeedDetail(String feedSn);
	List<Feed> selectNextFeedList(String currentFeedCrtDt, int limit, String context, String userNo);
	
	Map<String, Object> selectFollowingFeedList(String followerUserNo, int page, int size, String sortOrder);
	
	void insertFeed(String feedCn, String hashtags, List<MultipartFile> imageFiles, LoginUser loginUser);
	
	boolean updateFeed(String feedSn, String feedCn, String hashtags, List<String> deleteImageSns, List<MultipartFile> newImageFiles, LoginUser loginUser);
	
	boolean deleteFeed(String feedSn, String userNo);

	Map<String, Object> addLike(String feedSn, String userNo);
	FeedComment addComment(String feedSn, String commentText, String userNo);
	boolean deleteComment(String feedCmntSn, String userNo);
	FeedComment updateComment(String feedCmntSn, String commentText, String userNo);
}