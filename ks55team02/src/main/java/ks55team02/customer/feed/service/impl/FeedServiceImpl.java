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

	@Override
	@Transactional
	public FeedComment addComment(String feedSn, String commentText, String userNo) {
		String lastSn = feedMapper.selectLastFeedCommentSn();
		int newNum = 1;
		if (lastSn != null) {
			newNum = Integer.parseInt(lastSn.replace("feed_cmnt_", "")) + 1;
		}
		String newCommentSn = "feed_cmnt_" + String.format("%03d", newNum);
		FeedComment newComment = new FeedComment();
		newComment.setFeedCmntSn(newCommentSn);
		newComment.setFeedSn(feedSn);
		newComment.setWrtrUserNo(userNo);
		newComment.setCmntCn(commentText);
		feedMapper.insertComment(newComment);
		return feedMapper.selectCommentBySn(newCommentSn);
	}

	// [신규] 댓글 삭제 로직
	@Override
	@Transactional
	public boolean deleteComment(String feedCmntSn, String userNo) {
		// [핵심] DB에서 실제 댓글 정보를 가져와 작성자 본인인지 서버에서 한번 더 확인합니다.
		FeedComment comment = feedMapper.selectCommentBySn(feedCmntSn);

		// 댓글이 존재하고, 요청한 사용자가 댓글 작성자와 일치하는 경우에만 삭제를 진행합니다.
		if (comment != null && comment.getWrtrUserNo().equals(userNo)) {
			feedMapper.deleteComment(feedCmntSn, userNo);
			return true; // 삭제 성공
		}

		return false; // 삭제 실패 (댓글이 없거나, 권한이 없음)
	}
	// [신규] 댓글 수정 로직
	@Override
	@Transactional
	public FeedComment updateComment(String feedCmntSn, String commentText, String userNo) {
		// [핵심] DB에서 실제 댓글 정보를 가져와 작성자 본인인지 서버에서 한번 더 확인합니다.
		FeedComment comment = feedMapper.selectCommentBySn(feedCmntSn);

		// 댓글이 존재하고, 요청한 사용자가 댓글 작성자와 일치하는 경우에만 수정을 진행합니다.
		if (comment != null && comment.getWrtrUserNo().equals(userNo)) {
			feedMapper.updateComment(feedCmntSn, commentText);
			// 수정된 최신 정보를 다시 조회하여 반환
			return feedMapper.selectCommentBySn(feedCmntSn);
		}
		
		// 권한이 없거나 댓글이 없는 경우 null 반환
		return null;
	}
}