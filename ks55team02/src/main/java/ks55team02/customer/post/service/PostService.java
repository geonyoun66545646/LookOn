package ks55team02.customer.post.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import ks55team02.customer.post.domain.Board;
import ks55team02.customer.post.domain.Post;
import ks55team02.customer.post.domain.PostComment;

public interface PostService {

    List<Board> selectBoardName();
    
    Post selectPostDetailByPostSn(String pstSn, String loginUserNo);
    
    void increaseViewCount(String pstSn);
    
    List<Post> selectPostListByBoardCd(String bbsClsfCd, int offset, int size);
    
    int selectPostListNumByBoardCd(String bbsClsfCd);
    
    void insertPost(Post post, List<MultipartFile> imageFiles);
    
    boolean updatePost(Post post, List<MultipartFile> newImageFiles, List<String> deleteImageSns, String loginUserNo);
    
    void deletePost(String pstSn);
    
    void insertComment(PostComment comment);
    
    void updateComment(PostComment comment);
    
    void deleteComment(String pstCmntSn);
    
    boolean addPostLike(String pstSn, String loginUserNo);
    
    int countInteractionsByPost(String pstSn);
}