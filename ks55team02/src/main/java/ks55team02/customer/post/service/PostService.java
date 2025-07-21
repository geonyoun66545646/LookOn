package ks55team02.customer.post.service;

import java.util.List;
import java.util.Map;
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
    
    // =======================================================
    // [수정] 게시글 생성/수정/삭제 메소드 시그니처 변경
    // =======================================================
    void insertPost(Post post, List<MultipartFile> imageFiles);
    
    boolean updatePost(Post post, List<MultipartFile> newImageFiles, List<String> deleteImageSns, String loginUserNo);
    
    void deletePost(String pstSn);
    
    void insertComment(PostComment comment);
    
    void updateComment(PostComment comment);
    
    void deleteComment(String pstCmntSn);
    
    Map<String, Object> togglePostLike(String pstSn, String loginUserNo);
}