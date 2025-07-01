package ks55team02.customer.board.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ks55team02.customer.board.domain.Board;

@Service
public interface BoardService {

	// 게시판 목록 조회
	List<Board> selectBoardName();
	
}
