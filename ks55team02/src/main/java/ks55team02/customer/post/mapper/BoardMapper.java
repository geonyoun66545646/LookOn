package ks55team02.customer.post.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.customer.post.domain.Board;

@Mapper
public interface BoardMapper {

	// 게시판 목록 조회
	List<Board> selectBoardName();
	
}
