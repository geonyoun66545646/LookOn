package ks55team02.customer.post.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ks55team02.customer.post.domain.Board;

@Service
public interface BoardService {

	// 게시판 목록 조회
	List<Board> selectBoardName();
	
}
