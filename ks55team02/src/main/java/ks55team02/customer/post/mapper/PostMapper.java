package ks55team02.customer.post.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.customer.post.domain.Comment;
import ks55team02.customer.post.domain.Post;

@Mapper
public interface PostMapper {

	// 게시글 조회수 증가
	int updateViewCount(String pstSn);
	
	// 게시판 글 목록 조회
	List<Post> selectPostListByBoardCd(
			@Param("bbsClsfCd") String bbsClsfCd,
	        @Param("offset") int offset,
	        @Param("limit") int limit);

	// 특정 게시판 게시글 갯수 조회
	int selectPostListNumByBoardCd(@Param("bbsClsfCd") String bbsClsfCd);

	// 특정 게시글 상세 조회
	Post selectPostDetailByPstSn(String pstSn);
	
	// 특정 게시글의 댓글 목록 조회
	List<Comment> selectCommentListByPstSn(String pstSn);
	
	// 게시글 일련번호 생성
	Integer selectMaxPostNumber();
	
	// 게시글 등록
	int insertPost(Post post);
	
	// 댓글 일련번호 생성
	Integer selectMaxCommentNumber();
	
	// 댓글 등록
	int insertComment(Comment comment);
	
	// 게시글 수정
	int updatePost(Post post);
	
	// 게시글 삭제
	int deletePost(@Param("pstSn") String pstSn);
	
	// 댓글 수정
	int updateComment(Comment comment);
	
	// 댓글 삭제
	int deleteComment(@Param("pstCmntSn") String pstCmntSn);

}
