package ks55team02.customer.post.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.customer.post.domain.Comment;
import ks55team02.customer.post.domain.Post;
import ks55team02.customer.post.mapper.PostMapper;
import ks55team02.customer.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

	private final PostMapper postMapper;
	

	// 게시판 글 목록 조회
	@Override
	public List<Post> selectPostListByBoardCd(String bbsClsfCd, int offset, int size) {
		List<Post> postList = postMapper.selectPostListByBoardCd(bbsClsfCd, offset, size);
		return postList;
	}

	// 특정 게시판 게시글 갯수 조회
	@Override
	public int selectPostListNumByBoardCd(String bbsClsfCd) {
		int postNum = postMapper.selectPostListNumByBoardCd(bbsClsfCd);
		return postNum;
	}

	// 특정 게시글 상세 조회
	@Override
	public Post selectPostDetailByPostSn(String pstSn) {
		Post post = postMapper.selectPostDetailByPstSn(pstSn);
		List<Comment> comment = postMapper.selectCommentListByPstSn(pstSn);
		post.setComment(comment);
		postMapper.updateViewCount(pstSn);
		return post;
	}

	// 게시글 등록
	@Override
	public void insertPost(Post post) {
		Integer postMaxNum = postMapper.selectMaxPostNumber();
		int nextNum = postMaxNum + 1;
		String newPostNum = String.format("post_%d", nextNum);
		post.setPstSn(newPostNum);
		
		int result = postMapper.insertPost(post);
		String insertResult = "게시글 등록 실패";
		if(result > 0) insertResult = "게시글 등록 성공";
		
		log.info(insertResult);
	};
	
	// 댓글 등록
	@Override
	public void insertComment(Comment comment) {
		Integer commentMaxNum = postMapper.selectMaxCommentNumber();
		int nextNum = commentMaxNum + 1;
		String newCommentNum = String.format("cmnt_%d", nextNum);
		comment.setPstCmntSn(newCommentNum);
		
		int result = postMapper.insertComment(comment);
		String insertResult = "댓글 등록 실패";
		if(result > 0) insertResult = "댓글 등록 성공";
		
		log.info(insertResult);
	}
	
	// 게시글 수정
	@Override
	public void updatePost(Post post) {
		int result = postMapper.updatePost(post);
		String updateResult = "게시글 수정 실패";
		if(result > 0) updateResult = "게시글 수정 성공";
		
		log.info(updateResult);
	}
	
	// 게시글 삭제
	@Override
	public void deletePost(String pstSn) {
	    postMapper.deletePost(pstSn);
	}
	
	// 댓글 수정
	@Override
	public void updateComment(Comment comment) {
		int result = postMapper.updateComment(comment);
		String updateResult = "댓글 수정 실패";
		if(result > 0) updateResult = "댓글 수정 성공";
		
		log.info(updateResult);
	}
	
	// 댓글 삭제
	@Override
	public void deleteComment(String pstCmntSn) {
		postMapper.deleteComment(pstCmntSn);
	}
}
