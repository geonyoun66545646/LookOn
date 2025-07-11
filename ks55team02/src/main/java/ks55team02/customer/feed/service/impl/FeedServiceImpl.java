package ks55team02.customer.feed.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ks55team02.customer.feed.domain.Feed;
import ks55team02.customer.feed.mapper.FeedMapper;
import ks55team02.customer.feed.service.FeedService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {
	
	private final FeedMapper feedMapper;
	
	
	// 피드 목록 조회
	@Override
    public Map<String, Object> selectFeedList(int page, int pageSize) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("limit", pageSize);
        paramMap.put("offset", (page - 1) * pageSize);

        List<Feed> feedList = feedMapper.selectFeedList(paramMap);
        int totalCount = feedMapper.selectFeedCount();

        boolean hasNext = (page * pageSize) < totalCount;

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("feedList", feedList);
        resultMap.put("hasNext", hasNext);

        return resultMap;
    }
	
	// 피드 상세 조회
    @Override
    public Feed selectFeedDetail(String feedSn) {
        return feedMapper.selectFeedDetail(feedSn);
    }
    
    // 피드 다음 페이지 조회
    @Override
    public Feed selectNextFeed(String currentFeedCrtDt, String wrtrUserNo) {
    	Map<String, Object> params = new HashMap<>();
    	params.put("currentFeedCrtDt", currentFeedCrtDt);
    	params.put("wrtrUserNo", wrtrUserNo);
        return feedMapper.selectNextFeed(params);
    }
    
    // 마이 피드 목록 조회
    @Override
    public Map<String, Object> selectFeedListByMe(String userNo, int page, int size) {
        // 1. DB에 요청할 개수 설정 (페이지 사이즈 + 1)
        int limit = size;
        int limitPlusOne = size + 1;
        int offset = (page - 1) * size;

        // 2. Mapper에 전달할 파라미터 Map 생성
        Map<String, Object> params = new HashMap<>();
        params.put("userNo", userNo); // XML의 #{userNo}와 매칭
        params.put("limitPlusOne", limitPlusOne); // XML의 #{limitPlusOne}와 매칭
        params.put("offset", offset); // XML의 #{offset}와 매칭
        
        // 3. DB에서 'size + 1' 만큼의 피드 목록 조회
        List<Feed> feedListWithExtra = feedMapper.selectFeedListByMe(params);

        // 4. 다음 페이지 존재 여부(hasNext) 판별
        boolean hasNext = feedListWithExtra.size() > limit;

        // 5. 뷰에 전달할 실제 피드 리스트 생성 (만약 더 있다면 마지막 항목은 제거)
        List<Feed> feedList = hasNext ? feedListWithExtra.subList(0, limit) : feedListWithExtra;

        // 6. 컨트롤러에 반환할 결과 Map 생성
        Map<String, Object> result = new HashMap<>();
        result.put("feedList", feedList);
        result.put("hasNext", hasNext);

        return result;
    }
}
