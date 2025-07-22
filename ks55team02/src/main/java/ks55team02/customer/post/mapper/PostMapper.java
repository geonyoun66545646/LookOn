package ks55team02.customer.post.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ks55team02.customer.post.domain.Board;
import ks55team02.customer.post.domain.Post;
import ks55team02.customer.post.domain.PostComment;
import ks55team02.customer.post.domain.PostImage;
import ks55team02.customer.post.domain.PostInteraction;

@Mapper
public interface PostMapper {
	
	List<Board> selectBoardName();
	
	Post selectPostDetailByPstSn(@Param("pstSn") String pstSn, @Param("loginUserNo") String loginUserNo);
	
	int updateViewCount(String pstSn);
	
	List<Post> selectPostListByBoardCd(@Param("bbsClsfCd") String bbsClsfCd, @Param("offset") int offset, @Param("limit") int limit);
	
	int selectPostListNumByBoardCd(@Param("bbsClsfCd") String bbsClsfCd);
	
	List<PostComment> selectCommentListByPstSn(String pstSn);
	
	Integer selectMaxPostNumber();
	
	int insertPost(Post post);
    
    int updatePost(Post post);
    
    int deletePost(@Param("pstSn") String pstSn);

    Integer selectMaxPostImageNumber();
    
    int insertPostImages(@Param("imageList") List<PostImage> imageList);
    
    int deletePostImagesBySn(@Param("imageSnList") List<String> imageSnList, @Param("delUserNo") String delUserNo);
    
    List<PostImage> selectPostImagesByPstSn(String pstSn);
    
    PostImage selectFirstPostImageByPstSn(String pstSn);
	
	Integer selectMaxCommentNumber();
	
	int insertComment(PostComment comment);
	
	int updateComment(PostComment comment);
	
	int deleteComment(@Param("pstCmntSn") String pstCmntSn);
	
    Integer selectMaxInterNumber();

    int checkLikeExists(@Param("pstSn") String pstSn, @Param("userNo") String userNo);

    void insertInteraction(PostInteraction interaction);

    int countInteractionsByPost(@Param("pstSn") String pstSn);
}