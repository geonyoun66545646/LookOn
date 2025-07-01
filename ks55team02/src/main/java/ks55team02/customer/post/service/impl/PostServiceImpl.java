package ks55team02.customer.post.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
		return (Post) postMapper.selectPostDetailByPostSn(pstSn);
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
}
