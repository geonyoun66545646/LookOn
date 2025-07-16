package ks55team02.customer.feed.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.customer.feed.domain.Feed;
import ks55team02.customer.feed.domain.FeedImage;

@Mapper
public interface FeedMapper {

	// 피드 목록 조회
	List<Feed> selectFeedList(@Param("userNo") String userNo,
						@Param("limit") int limit,
						@Param("offset") int offset);
	
	
	// 피드 목록 조회를 위한 갯수 조회
	int selectFeedCount(@Param("userNo") String userNo);
	
	// 피드 상세 정보 조회
	Feed selectFeedDetail(String feedSn);
	
	// 신규 다음페이지
	List<Feed> selectNextFeedList(@Param("currentFeedCrtDt") String currentFeedCrtDt,
						@Param("limit") int limit,
						@Param("context") String context,
						@Param("userNo") String userNo);
	
	// 피드 일련번호 생성 로직
	String selectLastFeedSn();
	
	// 피드 삽입
	int insertFeed(Feed feed);
	
	// 피드 이미지 일련번호 생성 로직
	String selectLastFeedImageSn();
	
	// 피드 이미지 삽입
	int insertFeedImages(List<FeedImage> imageList);
}
