package ks55team02.customer.feed.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import ks55team02.customer.feed.domain.Feed;
import ks55team02.customer.feed.domain.FeedComment;
import ks55team02.customer.feed.domain.FeedImage;
import ks55team02.customer.feed.domain.FeedInteraction;
import ks55team02.customer.feed.domain.FeedTag;
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

	@Override
	public Map<String, Object> selectFeedList(String userNo, int page, int size, String sortOrder) {
	    int offset = (page - 1) * size;

	    // [수정] Mapper에 전달할 파라미터를 Map으로 구성
	    Map<String, Object> params = new HashMap<>();
	    params.put("userNo", userNo);
	    params.put("limit", size);
	    params.put("offset", offset);
	    params.put("sortOrder", sortOrder);
	    
	    List<Feed> feedList = feedMapper.selectFeedList(params);
	    int totalCount = feedMapper.selectFeedCount(userNo);
	    boolean hasNext = (offset + feedList.size()) < totalCount;

	    Map<String, Object> result = new HashMap<>();
	    result.put("feedList", feedList);
	    result.put("hasNext", hasNext);
	    result.put("totalCount", totalCount);
	    return result;
	}
	
    @Override
    public Map<String, Object> selectFollowingFeedList(String followerUserNo, int page, int size) {
        int offset = (page - 1) * size;
        List<Feed> feedList = feedMapper.selectFollowingFeedList(followerUserNo, size, offset);
        int totalCount = feedMapper.countFollowingFeeds(followerUserNo);
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
	public void insertFeed(String feedCn, String hashtags, List<MultipartFile> imageFiles, LoginUser loginUser) {
		String lastFeedSn = feedMapper.selectLastFeedSn();
		int newFeedNum = (lastFeedSn == null) ? 1 : Integer.parseInt(lastFeedSn.replace("feed_", "")) + 1;
		String newFeedSn = "feed_" + String.format("%03d", newFeedNum);
		Feed newFeed = new Feed();
		newFeed.setFeedSn(newFeedSn);
		newFeed.setFeedCn(feedCn);
		newFeed.setWrtrUserNo(loginUser.getUserNo());
		feedMapper.insertFeed(newFeed);
		
		saveImages(newFeedSn, imageFiles);
		
		processHashtags(newFeedSn, hashtags, loginUser);
	}
	
	@Override
	@Transactional
	public boolean updateFeed(String feedSn, String feedCn, String hashtags, List<String> deleteImageSns, List<MultipartFile> newImageFiles, LoginUser loginUser) {
	    Feed existingFeed = feedMapper.selectFeedDetail(feedSn);
	    if (existingFeed == null || !existingFeed.getWrtrUserNo().equals(loginUser.getUserNo())) {
	        log.warn("피드 수정 권한 없음: feedSn={}, userNo={}", feedSn, loginUser.getUserNo());
	        return false;
	    }

	    Feed feedToUpdate = new Feed();
	    feedToUpdate.setFeedSn(feedSn);
	    feedToUpdate.setFeedCn(feedCn);
	    feedMapper.updateFeed(feedToUpdate);

	    if (deleteImageSns != null && !deleteImageSns.isEmpty()) {
	        feedMapper.deleteFeedImagesBySn(deleteImageSns, loginUser.getUserNo());
	    }
	    
	    saveImages(feedSn, newImageFiles);
	    
	    feedMapper.deleteTagsByFeedSn(feedSn);
	    processHashtags(feedSn, hashtags, loginUser);

	    return true;
	}

	@Override
	@Transactional
	public boolean deleteFeed(String feedSn, String userNo) {
	    Feed feed = feedMapper.selectFeedDetail(feedSn);
	    if (feed == null || !feed.getWrtrUserNo().equals(userNo)) {
	        return false;
	    }
	    feedMapper.deleteFeedLogically(feedSn, userNo);
	    return true;
	}

	private void saveImages(String feedSn, List<MultipartFile> imageFiles) {
	    if (imageFiles != null && !imageFiles.isEmpty()) {
	        List<FeedImage> imageListForDb = new ArrayList<>();
	        String lastFeedImgSn = feedMapper.selectLastFeedImageSn();
	        int newFeedImgNum = (lastFeedImgSn == null) ? 1 : Integer.parseInt(lastFeedImgSn.replace("feed_img_", "")) + 1;
	        int sortOrder = 0;

	        for (MultipartFile file : imageFiles) {
	            if (file != null && !file.isEmpty()) {
	                FileDetail fileDetail = filesUtils.saveFile(file, "feeds");
	                if (fileDetail != null) {
	                    FeedImage feedImage = new FeedImage();
	                    String newImgSn = "feed_img_" + String.format("%03d", newFeedImgNum++);
	                    feedImage.setFeedImgSn(newImgSn);
	                    feedImage.setFeedSn(feedSn);
	                    feedImage.setImgFilePathNm(fileDetail.getSavedPath());
	                    feedImage.setFeedImgSortSn(String.valueOf(sortOrder++)); 
	                    imageListForDb.add(feedImage);
	                }
	            }
	        }

	        if (!imageListForDb.isEmpty()) {
	            feedMapper.insertFeedImages(imageListForDb);
	        }
	    }
	}

	private void processHashtags(String feedSn, String hashtags, LoginUser loginUser) {
	    if (!StringUtils.hasText(hashtags)) {
	        return;
	    }
	    List<String> tagNames = Arrays.stream(hashtags.replace("#", "").split("\\s+"))
	                                  .filter(StringUtils::hasText)
	                                  .distinct()
	                                  .collect(Collectors.toList());

	    if (tagNames.isEmpty()) {
	        return;
	    }
	    
	    String lastTagNumStr = feedMapper.selectLastTagSn();
	    AtomicInteger lastTagSnNum = new AtomicInteger(lastTagNumStr == null ? 1 : Integer.parseInt(lastTagNumStr) + 1);
	    
	    String lastFeedTagNumStr = feedMapper.selectLastFeedTagSn();
	    AtomicInteger lastFeedTagSnNum = new AtomicInteger(lastFeedTagNumStr == null ? 1 : Integer.parseInt(lastFeedTagNumStr) + 1);
	    
	    List<FeedTag> feedTagList = new ArrayList<>();
	    
	    for (String tagName : tagNames) {
	        FeedTag tag = feedMapper.findTagByName(tagName);
	        if (tag == null) {
	            tag = new FeedTag();
	            String newTagSn = "tag_sn_" + String.format("%03d", lastTagSnNum.getAndIncrement());
	            tag.setTagSn(newTagSn);
	            tag.setTagNm(tagName);
	            tag.setCreatrUserNo(loginUser.getUserNo());
	            feedMapper.insertTag(tag);
	        }
	        
	        FeedTag feedTag = new FeedTag();
	        String newFeedTagSn = "feed_tag_sn_" + String.format("%03d", lastFeedTagSnNum.getAndIncrement());
	        feedTag.setFeedTagSn(newFeedTagSn);
	        feedTag.setFeedSn(feedSn);
	        feedTag.setTagSn(tag.getTagSn());
	        feedTagList.add(feedTag);
	    }
	    
	    if (!feedTagList.isEmpty()) {
	        feedMapper.insertFeedTags(feedTagList);
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

	@Override
	@Transactional
	public boolean deleteComment(String feedCmntSn, String userNo) {
		FeedComment comment = feedMapper.selectCommentBySn(feedCmntSn);
		if (comment != null && comment.getWrtrUserNo().equals(userNo)) {
			feedMapper.deleteComment(feedCmntSn, userNo);
			return true;
		}
		return false;
	}
	
	@Override
	@Transactional
	public FeedComment updateComment(String feedCmntSn, String commentText, String userNo) {
		FeedComment comment = feedMapper.selectCommentBySn(feedCmntSn);
		if (comment != null && comment.getWrtrUserNo().equals(userNo)) {
			feedMapper.updateComment(feedCmntSn, commentText);
			return feedMapper.selectCommentBySn(feedCmntSn);
		}
		return null;
	}
}