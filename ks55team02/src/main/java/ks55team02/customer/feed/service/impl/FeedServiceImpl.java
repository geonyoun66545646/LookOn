package ks55team02.customer.feed.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import ks55team02.customer.feed.domain.Feed;
import ks55team02.customer.feed.domain.FeedComment;
import ks55team02.customer.feed.domain.FeedImage;
import ks55team02.customer.feed.domain.FeedInteraction;
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

	// ... (selectFeedList, selectFeedDetail 등 다른 메소드는 기존과 동일) ...
	@Override
    public Map<String, Object> selectFeedList(String userNo, int page, int size) {
		int offset = (page - 1) * size;
        List<Feed> feedList = feedMapper.selectFeedList(userNo, size, offset);
        int totalCount = feedMapper.selectFeedCount(userNo);
        boolean hasNext = (offset + feedList.size()) < totalCount;

        Map<String, Object> result = new HashMap<>();
        result.put("feedList", feedList);
        result.put("hasNext", hasNext);
        result.put("totalCount", totalCount);
        return result;
    }
	
    @Override
    public Feed selectFeedDetail(String feedSn) {
        return feedMapper.selectFeedDetail(feedSn);
    }
    
    @Override
    public List<Feed> selectNextFeedList(String currentFeedCrtDt, int limit, String context, String userNo) {
        return feedMapper.selectNextFeedList(currentFeedCrtDt, limit, context, userNo);
    }
    
    @Override
    @Transactional
	public void insertFeed(String feedCn, List<MultipartFile> imageFiles, LoginUser loginUser) {
		String lastFeedSn = feedMapper.selectLastFeedSn();
		int newFeedNum = 1;
		if (lastFeedSn != null) {
			newFeedNum = Integer.parseInt(lastFeedSn.replace("feed_", "")) + 1;
		}
		String newFeedSn = "feed_" + String.format("%03d", newFeedNum);
		Feed newFeed = new Feed();
		newFeed.setFeedSn(newFeedSn);
		newFeed.setFeedCn(feedCn);
		newFeed.setWrtrUserNo(loginUser.getUserNo());
		feedMapper.insertFeed(newFeed);
		if (imageFiles != null && !imageFiles.isEmpty() && !imageFiles.get(0).isEmpty()) {
			String lastFeedImgSn = feedMapper.selectLastFeedImageSn();
			int newFeedImgNum = 1;
			if (lastFeedImgSn != null) {
				newFeedImgNum = Integer.parseInt(lastFeedImgSn.replace("feed_img_", "")) + 1;
			}
			List<FeedImage> imageListForDb = new ArrayList<>();
			for (int i = 0; i < imageFiles.size(); i++) {
				MultipartFile file = imageFiles.get(i);
				if (file == null || file.isEmpty()) continue;
				FileDetail fileDetail = filesUtils.saveFile(file, "feeds");
				if (fileDetail != null) {
					FeedImage feedImage = new FeedImage();
					String newFeedImgSn = "feed_img_" + String.format("%03d", (newFeedImgNum + i));
					feedImage.setFeedImgSn(newFeedImgSn);
					feedImage.setFeedSn(newFeedSn);
					feedImage.setImgFilePathNm(fileDetail.getSavedPath());
					feedImage.setFeedImgSortSn(String.valueOf(i));
					imageListForDb.add(feedImage);
				}
			}
			if (!imageListForDb.isEmpty()) {
				feedMapper.insertFeedImages(imageListForDb);
			}
		}
	}
	
	@Override
	@Transactional
	public Map<String, Object> addLike(String feedSn, String userNo) {
		String existingLike = feedMapper.findLikeByFeedSnAndUserNo(feedSn, userNo);
		if (existingLike == null) {
			String lastSn = feedMapper.selectLastFeedInteractionSn();
			int newNum = 1;
			if (lastSn != null) {
				newNum = Integer.parseInt(lastSn.replace("feed_intrcn_", "")) + 1;
			}
			String newSn = "feed_intrcn_" + String.format("%03d", newNum);
			FeedInteraction like = new FeedInteraction();
			like.setFeedIntractSn(newSn);
			like.setFeedSn(feedSn);
			like.setUserNo(userNo);
			feedMapper.insertLike(like);
		}
		int likeCount = feedMapper.countLikes(feedSn);
		Map<String, Object> result = new HashMap<>();
		result.put("likeCount", likeCount);
		return result;
	}

	// [신규] 댓글 추가 로직
	@Override
	@Transactional
	public FeedComment addComment(String feedSn, String commentText, String userNo) {
		// 1. 새로운 댓글 PK 생성
		String lastSn = feedMapper.selectLastFeedCommentSn();
		int newNum = 1;
		if (lastSn != null) {
			newNum = Integer.parseInt(lastSn.replace("feed_cmnt_", "")) + 1;
		}
		String newCommentSn = "feed_cmnt_" + String.format("%03d", newNum);

		// 2. DB에 삽입할 댓글 객체 생성
		FeedComment newComment = new FeedComment();
		newComment.setFeedCmntSn(newCommentSn);
		newComment.setFeedSn(feedSn);
		newComment.setWrtrUserNo(userNo);
		newComment.setCmntCn(commentText);

		// 3. DB에 INSERT
		feedMapper.insertComment(newComment);

		// 4. [핵심] 방금 삽입한 댓글의 완전한 정보를 다시 조회하여 반환
		//    (이렇게 해야 프론트에서 작성자 닉네임, 프로필 등을 바로 표시할 수 있음)
		return feedMapper.selectCommentBySn(newCommentSn);
	}
}