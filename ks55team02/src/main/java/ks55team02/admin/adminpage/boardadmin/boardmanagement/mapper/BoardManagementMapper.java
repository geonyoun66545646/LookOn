package ks55team02.admin.adminpage.boardadmin.boardmanagement.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import ks55team02.customer.post.domain.Board; // DTO 재사용 확정 시 주석 해제

@Mapper
public interface BoardManagementMapper {
	List<Board> selectBoardList();

	Board selectBoardByCode(String boardCode);

	int insertBoard(Board board);

	int updateBoard(Board board);

	int deleteBoard(String boardCode);

	int restoreBoard(String boardCode);
}