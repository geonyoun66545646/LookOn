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
	void insertFeed(String feedCn, List<MultipartFile> imageFiles, LoginUser loginUser);
	Map<String, Object> addLike(String feedSn, String userNo);
	FeedComment addComment(String feedSn, String commentText, String userNo);

	// [신규] 댓글 삭제 (권한 확인 포함)
	boolean deleteComment(String feedCmntSn, String userNo);
	// [신규] 댓글 수정 (권한 확인 포함)
	FeedComment updateComment(String feedCmntSn, String commentText, String userNo);
}