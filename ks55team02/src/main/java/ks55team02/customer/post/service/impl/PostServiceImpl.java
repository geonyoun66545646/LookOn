package ks55team02.customer.post.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import ks55team02.customer.post.domain.Board;
import ks55team02.customer.post.domain.Post;
import ks55team02.customer.post.domain.PostComment;
import ks55team02.customer.post.domain.PostImage;
import ks55team02.customer.post.domain.PostInteraction;
import ks55team02.customer.post.mapper.PostMapper;
import ks55team02.customer.post.service.PostService;
import ks55team02.util.FileDetail;
import ks55team02.util.FilesUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final FilesUtils filesUtils;

    @Override
    public List<Board> selectBoardName() {
        return postMapper.selectBoardName();
    }

    @Override
    @Transactional(readOnly = true)
    public Post selectPostDetailByPostSn(String pstSn, String loginUserNo) {
        return postMapper.selectPostDetailByPstSn(pstSn, loginUserNo);
    }
    
    @Override
    @Transactional
    public void increaseViewCount(String pstSn) {
        postMapper.updateViewCount(pstSn);
    }
    
    @Override
    public List<Post> selectPostListByBoardCd(String bbsClsfCd, int offset, int size) {
        return postMapper.selectPostListByBoardCd(bbsClsfCd, offset, size);
    }

    @Override
    public int selectPostListNumByBoardCd(String bbsClsfCd) {
        return postMapper.selectPostListNumByBoardCd(bbsClsfCd);
    }

    @Override
    @Transactional
    public void insertPost(Post post, List<MultipartFile> imageFiles) {
        Integer maxPostNum = postMapper.selectMaxPostNumber();
        int newPostNum = (maxPostNum == null) ? 1 : maxPostNum + 1;
        String newPstSn = "post_" + newPostNum;
        post.setPstSn(newPstSn);
        
        postMapper.insertPost(post);

        savePostImages(newPstSn, imageFiles);
    }

    @Override
    @Transactional
    public boolean updatePost(Post post, List<MultipartFile> newImageFiles, List<String> deleteImageSns, String loginUserNo) {
        Post existingPost = postMapper.selectPostDetailByPstSn(post.getPstSn(), loginUserNo);
        if (existingPost == null || !existingPost.getWrtrUserNo().equals(loginUserNo)) {
            log.warn("게시글 수정 권한 없음: pstSn={}, userNo={}", post.getPstSn(), loginUserNo);
            return false;
        }

        postMapper.updatePost(post);

        if (deleteImageSns != null && !deleteImageSns.isEmpty()) {
            postMapper.deletePostImagesBySn(deleteImageSns, loginUserNo);
        }
        
        savePostImages(post.getPstSn(), newImageFiles);

        return true;
    }

    private void savePostImages(String pstSn, List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty() || imageFiles.stream().allMatch(MultipartFile::isEmpty)) {
            return;
        }

        List<PostImage> imageListForDb = new ArrayList<>();
        
        Integer maxImgNum = postMapper.selectMaxPostImageNumber();
        int newImgNum = (maxImgNum == null) ? 1 : maxImgNum + 1;
        int sortOrder = 0;

        for (MultipartFile file : imageFiles) {
            if (file != null && !file.isEmpty()) {
                FileDetail fileDetail = filesUtils.saveFile(file, "posts");
                if (fileDetail != null) {
                    PostImage postImage = new PostImage();
                    String newImgSn = "post_img_" + newImgNum++;
                    postImage.setPstImgSn(newImgSn);
                    postImage.setPstSn(pstSn);
                    postImage.setImgFilePathNm(fileDetail.getSavedPath());
                    postImage.setPstImgSortSn(sortOrder++);
                    
                    imageListForDb.add(postImage);
                }
            }
        }

        if (!imageListForDb.isEmpty()) {
            postMapper.insertPostImages(imageListForDb);
        }
    }

    @Override
    public void deletePost(String pstSn) {
        postMapper.deletePost(pstSn);
    }
    
    @Override
    public void insertComment(PostComment comment) {
        Integer commentMaxNum = postMapper.selectMaxCommentNumber();
        int nextNum = (commentMaxNum == null) ? 1 : commentMaxNum + 1;
        String newCommentNum = "cmnt_" + nextNum;
        comment.setPstCmntSn(newCommentNum);
        postMapper.insertComment(comment);
    }

    @Override
    public void updateComment(PostComment comment) {
        postMapper.updateComment(comment);
    }
    
    @Override
    public void deleteComment(String pstCmntSn) {
        postMapper.deleteComment(pstCmntSn);
    }

    @Override
    @Transactional
    public Map<String, Object> togglePostLike(String pstSn, String loginUserNo) {
        PostInteraction existingLike = postMapper.findInteractionByUserAndPost(pstSn, loginUserNo);
        boolean isLiked;

        if (existingLike == null) {
            Integer maxNum = postMapper.selectMaxInterNumber();
            int nextNum = (maxNum == null) ? 1 : maxNum + 1;
            String newSn = "pst_itrct_" + nextNum;
            
            PostInteraction newLike = new PostInteraction();
            newLike.setPstIntractSn(newSn);
            newLike.setPstSn(pstSn);
            newLike.setUserNo(loginUserNo);
            
            postMapper.insertInteraction(newLike);
            isLiked = true;

        } else {
            if (existingLike.getRtrcnDt() != null) {
                postMapper.revertLike(existingLike.getPstIntractSn());
                isLiked = true;
            } else {
                postMapper.cancelLike(existingLike.getPstIntractSn());
                isLiked = false;
            }
        }

        int currentLikeCount = postMapper.countInteractionsByPost(pstSn);

        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);
        result.put("likeCount", currentLikeCount);
        return result;
    }
}