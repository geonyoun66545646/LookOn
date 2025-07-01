package ks55team02.customer.post.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ks55team02.customer.post.domain.Post;

@Service
public interface PostService {

	// 게시판 글 목록 조회
	List<Post> selectPostListByBoardCd(String bbsClsfCd, int offset, int size);

	// 특정 게시판 게시글 갯수 조회
	int selectPostListNumByBoardCd(String bbsClsfCd);

	// 특정 게시글 상세 조회
	Post selectPostDetailByPostSn(String pstSn);
	
	// 게시글 등록
	void insertPost(Post post);

	// 게시글 수정
	void updatePost(Post post);
	
	// 게시글 삭제
	void deletePost(String pstSn);

}
