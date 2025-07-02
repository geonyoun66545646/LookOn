package ks55team02.customer.post.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import ks55team02.customer.post.domain.Board;
import ks55team02.customer.post.mapper.BoardMapper;
import ks55team02.customer.post.service.BoardService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

	private final BoardMapper boardMapper;
	// 게시판 목록 조회
	@Override
	public List<Board> selectBoardName() {
		return boardMapper.selectBoardName();
	}
}
