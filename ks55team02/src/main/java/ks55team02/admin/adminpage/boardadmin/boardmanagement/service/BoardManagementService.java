package ks55team02.admin.adminpage.boardadmin.boardmanagement.service;

import java.util.List;
 import ks55team02.customer.post.domain.Board; // DTO 재사용 확정 시 주석 해제

public interface BoardManagementService {
    
     List<Board> getBoardList();
    
     Board getBoardByCode(String boardCode);
    
     void addBoard(Board board, String adminUserNo);
    
     boolean updateBoard(Board board);

     boolean deleteBoard(String boardCode);
     
     boolean restoreBoard(String boardCode);
}