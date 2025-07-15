package ks55team02.customer.feed.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ks55team02.customer.feed.domain.Feed;
import ks55team02.customer.feed.mapper.FeedMapper;
import ks55team02.customer.feed.service.FeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedServiceImpl implements FeedService {
	
	private final FeedMapper feedMapper;
	
	
	// 피드 목록 조회
	@Override
    public Map<String, Object> selectFeedList(String userNo, int page, int size) {
		
		int limit = size;
		int limitPlusOne = size + 1;
		int offset = (page - 1) * size;

        List<Feed> feedListWithExtra = feedMapper.selectFeedList(userNo, limitPlusOne, offset);
        
        boolean hasNext = feedListWithExtra.size() > limit;
        
        List<Feed> feedList = hasNext ? feedListWithExtra.subList(0, limit) : feedListWithExtra;

        Map<String, Object> result = new HashMap<>();
        result.put("feedList", feedList);
        result.put("hasNext", hasNext);

        return result;
    }
	
	// 피드 상세 조회
    @Override
    public Feed selectFeedDetail(String feedSn) {
        return feedMapper.selectFeedDetail(feedSn);
    }
    
    // 피드 다음 페이지 조회(신규)
    @Override
    public List<Feed> selectNextFeedList(String currentFeedCrtDt, int limit, String context, String userNo) {

        return feedMapper.selectNextFeedList(currentFeedCrtDt, limit, context, userNo);
    }
    

}
