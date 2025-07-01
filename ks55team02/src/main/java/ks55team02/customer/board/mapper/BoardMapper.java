package ks55team02.customer.board.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.customer.board.domain.Board;

@Mapper
public interface BoardMapper {

	// 게시판 목록 조회
	List<Board> selectBoardName();
	
}
