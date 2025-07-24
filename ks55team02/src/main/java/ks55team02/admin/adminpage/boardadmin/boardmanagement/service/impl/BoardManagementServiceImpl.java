package ks55team02.admin.adminpage.boardadmin.boardmanagement.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.boardadmin.boardmanagement.mapper.BoardManagementMapper;
import ks55team02.admin.adminpage.boardadmin.boardmanagement.service.BoardManagementService;
import ks55team02.customer.post.domain.Board;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardManagementServiceImpl implements BoardManagementService {

    private final BoardManagementMapper boardManagementMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Board> selectBoardList() {
        return boardManagementMapper.selectBoardList();
    }

    @Override
    @Transactional(readOnly = true)
    public Board selectBoardByCode(String boardCode) {
        return boardManagementMapper.selectBoardByCode(boardCode);
    }

    @Override
    public void insertBoard(Board board, String adminUserNo) {
    	board.setBbsCreatrUserNo(adminUserNo);
        boardManagementMapper.insertBoard(board);
    }

    @Override
    public boolean updateBoard(Board board) {
        return boardManagementMapper.updateBoard(board) > 0;
    }

    @Override
    public boolean deleteBoard(String boardCode) {
        return boardManagementMapper.deleteBoard(boardCode) > 0;
    }
    
    // [신규] 복구 메소드 구현
    @Override
    public boolean restoreBoard(String boardCode) {
        return boardManagementMapper.restoreBoard(boardCode) > 0;
    }
}