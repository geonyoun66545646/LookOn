package ks55team02.customer.feed.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.customer.feed.domain.Feed;
import ks55team02.customer.feed.domain.FeedComment;
import ks55team02.customer.feed.domain.FeedImage;
import ks55team02.customer.feed.domain.FeedInteraction;
import ks55team02.customer.feed.domain.FeedTag;

@Mapper
public interface FeedMapper {

	// 피드 목록/상세 관련
	List<Feed> selectFeedList(@Param("userNo") String userNo, @Param("limit") int limit, @Param("offset") int offset);
	int selectFeedCount(@Param("userNo") String userNo);
	Feed selectFeedDetail(String feedSn);
	List<Feed> selectNextFeedList(@Param("currentFeedCrtDt") String currentFeedCrtDt, @Param("limit") int limit, @Param("context") String context, @Param("userNo") String userNo);
	List<FeedImage> selectImagesByFeedSn(String feedSn);
	List<FeedTag> selectTagsByFeedSn(String feedSn);
	List<FeedComment> selectCommentsByFeedSn(String feedSn);

	// 피드 작성 관련
	String selectLastFeedSn();
	int insertFeed(Feed feed);
	String selectLastFeedImageSn();
	int insertFeedImages(List<FeedImage> imageList);
	
	// 좋아요 기능 관련
	String findLikeByFeedSnAndUserNo(@Param("feedSn") String feedSn, @Param("userNo") String userNo);
	String selectLastFeedInteractionSn();
	int countLikes(String feedSn);
	int insertLike(FeedInteraction like);

	// 댓글 작성 기능 관련
	String selectLastFeedCommentSn();
	int insertComment(FeedComment comment);
	FeedComment selectCommentBySn(String feedCmntSn);
	
	// [신규] 댓글 삭제 (논리적)
	int deleteComment(@Param("feedCmntSn") String feedCmntSn, @Param("delUserNo") String delUserNo);
	// [신규] 댓글 수정
	int updateComment(@Param("feedCmntSn") String feedCmntSn, @Param("commentText") String commentText);
}