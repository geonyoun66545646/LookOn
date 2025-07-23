package ks55team02.admin.adminpage.boardadmin.boardmanagement.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import ks55team02.customer.post.domain.Board; // DTO 재사용 확정 시 주석 해제

@Mapper
public interface BoardManagementMapper {
	List<Board> getBoardList();

	Board getBoardByCode(String boardCode);

    // [수정] void를 int로 변경하여 처리된 행의 수를 반환하도록 명시
    void addBoard(Board board);

    // [수정] 반환 타입을 int로 명시
    int updateBoard(Board board);

    // [수정] 반환 타입을 int로 명시
    int deleteBoard(String boardCode);
    
    int restoreBoard(String boardCode);
}