package ks55team02.customer.feed.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.customer.feed.domain.Feed;

@Mapper
public interface FeedMapper {

	// 피드 목록 조회
	List<Feed> selectFeedList(Map<String, Object> paramMap);
	
	// 피드 목록 조회를 위한 갯수 조회
	int selectFeedCount();
	
	// 피드 상세 정보 조회
	Feed selectFeedDetail(String feedSn);
	
	// 현재 피드보다 오래된 다음 피드 1개를 조회(무한 스크롤)
	Feed selectNextFeed(Map<String, Object> params);
	
	// 마이 피드 목록 조회
	List<Feed> selectFeedListByMe(Map<String, Object> params);
}
