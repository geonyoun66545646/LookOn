package ks55team02.customer.post.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ks55team02.customer.post.domain.PostComment;
import ks55team02.customer.post.domain.Post;
import ks55team02.customer.post.domain.PostInteraction;

@Service
public interface PostService {
	
	// 추천수 증가
	void insertInterCount(PostInteraction interaction);

	// 게시판 글 목록 조회
	List<Post> selectPostListByBoardCd(String bbsClsfCd, int offset, int size);

	// 특정 게시판 게시글 갯수 조회
	int selectPostListNumByBoardCd(String bbsClsfCd);

	// 특정 게시글 상세 조회
	Post selectPostDetailByPostSn(String pstSn);
	
	// 게시글 등록
	void insertPost(Post post);
	
	// 댓글 등록 
	void insertComment(PostComment comment);

	// 게시글 수정
	void updatePost(Post post);
	
	// 게시글 삭제
	void deletePost(String pstSn);
	
	// 댓글 수정
	void updateComment(PostComment comment);

	// 댓글 삭제
	void deleteComment(String pstCmntSn);
}
