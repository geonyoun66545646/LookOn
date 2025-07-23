package ks55team02.admin.adminpage.boardadmin.boardmanagement.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*; // 와일드카드 임포트
import ks55team02.admin.adminpage.boardadmin.boardmanagement.service.BoardManagementService;
import ks55team02.customer.login.domain.LoginUser; // LoginUser DTO 임포트
import ks55team02.customer.post.domain.Board;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/adminpage/boardadmin")
@RequiredArgsConstructor
@Slf4j
public class BoardManagementController {

    private final BoardManagementService boardManagementService;

    @GetMapping("/boardManagement") 
    public String boardList(Model model) {
        List<Board> boardList = boardManagementService.getBoardList();
        model.addAttribute("boardList", boardList);
        model.addAttribute("title", "게시판 관리");
        return "admin/adminpage/boardadmin/boardmanagement/adminBoardList";
    }
    
    // --- API 영역 ---

    @GetMapping("/boardManagement/boards/{boardCode}")
    @ResponseBody
    public ResponseEntity<Board> getBoardDetails(@PathVariable String boardCode) {
        Board board = boardManagementService.getBoardByCode(boardCode);
        if (board != null) {
            return ResponseEntity.ok(board);
        }
        return ResponseEntity.notFound().build();
    }
    
    // [최종 수정] 세션 정보를 받아 Service로 전달
    @PostMapping("/boardManagement/boards")
    @ResponseBody
    public ResponseEntity<?> addBoard(@RequestBody Board board,
                                      @SessionAttribute(name="loginUser", required = false) LoginUser loginAdmin) {
        try {
            if (loginAdmin == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("관리자 로그인이 필요합니다.");
            }
            // 권한 체크 (선택 사항이지만 추천)
            String userGrade = loginAdmin.getMbrGrdCd();
            if (!"grd_cd_0".equals(userGrade) && !"grd_cd_1".equals(userGrade)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("게시판을 생성할 권한이 없습니다.");
            }
            
            String adminUserNo = loginAdmin.getUserNo();
            boardManagementService.addBoard(board, adminUserNo); // Service에 관리자 번호 전달
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("게시판 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시판 생성 중 오류가 발생했습니다.");
        }
    }

    @PutMapping("/boardManagement/boards")
    @ResponseBody
    public ResponseEntity<?> updateBoard(@RequestBody Board board) {
        try {
            boolean isSuccess = boardManagementService.updateBoard(board);
            if (isSuccess) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("수정할 게시판을 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("게시판 수정 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시판 수정 중 오류가 발생했습니다.");
        }
    }

    @DeleteMapping("/boardManagement/boards/{boardCode}")
    @ResponseBody
    public ResponseEntity<?> deleteBoard(@PathVariable String boardCode) {
        try {
            boolean isSuccess = boardManagementService.deleteBoard(boardCode);
            if (isSuccess) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("삭제할 게시판을 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("게시판 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시판 삭제 중 오류가 발생했습니다.");
        }
    }
    
    // [신규] 게시판 복구 API
    @PutMapping("/boardManagement/boards/{boardCode}/restore")
    @ResponseBody
    public ResponseEntity<?> restoreBoard(@PathVariable String boardCode) {
        try {
            boolean isSuccess = boardManagementService.restoreBoard(boardCode);
            if (isSuccess) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("복구할 게시판을 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("게시판 복구 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시판 복구 중 오류가 발생했습니다.");
        }
    }
}