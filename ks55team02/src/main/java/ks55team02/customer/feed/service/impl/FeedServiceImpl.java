package ks55team02.customer.feed.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import ks55team02.customer.feed.domain.Feed;
import ks55team02.customer.feed.domain.FeedImage;
import ks55team02.customer.feed.mapper.FeedMapper;
import ks55team02.customer.feed.service.FeedService;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.util.FileDetail;
import ks55team02.util.FilesUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedServiceImpl implements FeedService {
	
	private final FeedMapper feedMapper;
	private final FilesUtils filesUtils;

	
	// 피드 목록 조회
	@Override
    public Map<String, Object> selectFeedList(String userNo, int page, int size) {
		
		int limit = size;
		int limitPlusOne = size + 1;
		int offset = (page - 1) * size;

        List<Feed> feedListWithExtra = feedMapper.selectFeedList(userNo, limitPlusOne, offset);
        
        boolean hasNext = feedListWithExtra.size() > limit;
        
        List<Feed> feedList = hasNext ? feedListWithExtra.subList(0, limit) : feedListWithExtra;
        int totalCount = feedMapper.selectFeedCount(userNo);

        Map<String, Object> result = new HashMap<>();
        result.put("feedList", feedList);
        result.put("hasNext", hasNext);
        result.put("totalCount", totalCount);
        
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
    
    // 피드 등록
    @Override
    @Transactional
    public void insertFeed(String feedCn, List<MultipartFile> imageFiles, LoginUser loginUser) {

        // --- 1. 새로운 피드 PK 생성 ---
        // 작성자님의 기존 PK 생성 로직을 그대로 유지합니다.
        String lastFeedSn = feedMapper.selectLastFeedSn();
        int newFeedNum = 1;
        if (lastFeedSn != null && !lastFeedSn.isEmpty()) {
            try {
                int lastNum = Integer.parseInt(lastFeedSn.replace("feed_", ""));
                newFeedNum = lastNum + 1;
            } catch (NumberFormatException e) {
                // PK 생성 실패 시 트랜잭션 롤백을 위해 RuntimeException을 발생시킵니다.
                throw new RuntimeException("피드 번호 생성에 실패했습니다. lastFeedSn: " + lastFeedSn, e);
            }
        }
        String newFeedSn = "feed_" + String.format("%03d", newFeedNum);
        
        // --- 2. feeds 테이블에 삽입 ---
        Feed newFeed = new Feed();
        newFeed.setFeedSn(newFeedSn);
        newFeed.setFeedCn(feedCn);
        newFeed.setWrtrUserNo(loginUser.getUserNo());
        feedMapper.insertFeed(newFeed);

        // --- 3. 이미지 파일 처리 ---
        if (imageFiles != null && !imageFiles.isEmpty() && !imageFiles.get(0).isEmpty()) {

            // 이미지 PK 생성을 위한 로직 역시 그대로 유지합니다.
            String lastFeedImgSn = feedMapper.selectLastFeedImageSn();
            int newFeedImgNum = 1;
            if (lastFeedImgSn != null && !lastFeedImgSn.isEmpty()) {
                try {
                    int lastImgNum = Integer.parseInt(lastFeedImgSn.replace("feed_img_", ""));
                    newFeedImgNum = lastImgNum + 1;
                } catch (NumberFormatException e) {
                     throw new RuntimeException("피드 이미지 번호 생성에 실패했습니다. lastFeedImgSn: " + lastFeedImgSn, e);
                }
            }

            List<FeedImage> imageListForDb = new ArrayList<>();
            for (int i = 0; i < imageFiles.size(); i++) {
                MultipartFile file = imageFiles.get(i);
                if (file == null || file.isEmpty()) continue;

                String subDirectoryForDebug = "feeds";
                log.info("### 디버깅: filesUtils.saveFile()에 전달하는 subDirectory 값: {}", subDirectoryForDebug);
                
                // [Step 3: FilesUtils를 사용하여 파일 저장]
                // 복잡한 파일 처리 로직을 단 한 줄로 대체합니다.
                // "feeds"를 subDirectory로 전달하여 .../attachment/feeds/yyyyMMdd/image/ 경로에 저장합니다.
                FileDetail fileDetail = filesUtils.saveFile(file, "feeds");
                
                if (fileDetail != null) {
                    // [Step 4: DB에 저장할 정보 생성]
                    FeedImage feedImage = new FeedImage();

                    // 새로운 이미지 PK를 생성합니다.
                    String newFeedImgSn = "feed_img_" + String.format("%03d", (newFeedImgNum + i));
                    feedImage.setFeedImgSn(newFeedImgSn);
                    
                    feedImage.setFeedSn(newFeedSn);

                    // FilesUtils가 반환한 웹 접근 경로(savedPath)를 DB에 저장합니다.
                    feedImage.setImgFilePathNm(fileDetail.getSavedPath()); 
                    
                    feedImage.setFeedImgSortSn(String.valueOf(i)); // 이미지 순서
                    
                    imageListForDb.add(feedImage);
                }
            }
            
            if (!imageListForDb.isEmpty()) {
                // MyBatis의 foreach를 사용하여 이미지 정보를 일괄 삽입합니다.
                feedMapper.insertFeedImages(imageListForDb);
            }
        }
    }
}
