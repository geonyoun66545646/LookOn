package ks55team02.customer.post.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ks55team02.customer.post.domain.Post;
import ks55team02.customer.post.domain.PostComment;
import ks55team02.customer.post.domain.PostInteraction;
import ks55team02.customer.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/customer/post")
@RequiredArgsConstructor
@Slf4j
public class PostRestController {

   
    private final PostService postService;
    
    // ===================================================================
    // ★★★ 이관 및 표준화된 메소드 시작 ★★★
    // ===================================================================

    // 추천수 증가
    @PostMapping("/interactionInsert")
    public ResponseEntity<Map<String, Object>> insertInteraction(PostInteraction interaction) {
        Map<String, Object> response = new HashMap<>();
        try {
            postService.insertInterCount(interaction);
            response.put("result", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error increasing interaction count: {}", e.getMessage());
            response.put("result", "fail");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 댓글 삭제
    @DeleteMapping("/commentDelete/{pstCmntSn}")
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable String pstCmntSn) {
        Map<String, Object> response = new HashMap<>();
        try {
            postService.deleteComment(pstCmntSn);
            response.put("result", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting comment: {}", e.getMessage());
            response.put("result", "fail");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 게시글 삭제
    @DeleteMapping("/postDelete/{pstSn}")
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable String pstSn) {
        Map<String, Object> response = new HashMap<>();
        try {
            postService.deletePost(pstSn);
            response.put("result", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting post: {}", e.getMessage());
            response.put("result", "fail");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 게시글 수정 (이관 대상이 아니므로 삭제)
    // -> postUpdate는 글 수정 페이지(postWrite.html)의 form 제출로 처리되므로, PostController에 있어야 합니다.
    // -> 제가 실수했습니다. updatePost는 AJAX가 아닌 form 제출을 처리하는 로직일 수 있습니다. 
    // -> 확인 결과 postUpdate는 PostController에서 AJAX로 처리되고 있었습니다. 이관이 맞습니다.
    @PostMapping("/postUpdate")
	public ResponseEntity<Map<String, Object>> updatePost(Post post) {
        Map<String, Object> response = new HashMap<>();
	    try {
	        postService.updatePost(post);
	        response.put("result", "success");
            return ResponseEntity.ok(response);
	    } catch (Exception e) {
	        log.error("글 수정 처리 중 오류 발생: {}", e.getMessage());
            response.put("result", "fail");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}
    
    // 댓글 수정
    @PostMapping("/updateComment")
    public Map<String, Object> updateComment(@RequestBody PostComment comment) {
    	
    	Map<String, Object> response = new HashMap<>();
    	
    	try {
    		postService.updateComment(comment);
    		response.put("result", "success");
    	} catch(Exception e) {
    		response.put("result", "fail");
    		log.error("Error adding comment : {}", e.getMessage());
    	}
    	return response;
    }
    
	// 댓글 작성
	@PostMapping("/insertComment")
	public Map<String, Object> insertComment(PostComment comment) {
		
		Map<String, Object> response = new HashMap<>();

		try {
			postService.insertComment(comment);
			response.put("result", "success");
		} catch(Exception e) {
			response.put("result", "fail");
			log.error("Error adding comment : {}", e.getMessage());
		}
		
		return response;
	}
	
    @GetMapping("/list-data") // API임을 명시하는 새로운 경로
    public ResponseEntity<Map<String, Object>> selectPostListData(
            @RequestParam(required = false) String bbsClsfCd,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size, // 기본값을 10으로 설정
            @RequestParam(required = false) String keyword // 검색 기능까지 고려
    ) {
        // --- 이 로직은 기존 PostController.selectPostList에서 그대로 가져온 것입니다. ---

        // 1. 게시판 이름 조회 (이 로직은 BoardService가 필요하므로 주입받아야 합니다)
        // (만약 BoardService 주입이 어렵다면, 이 부분은 제외하고 프론트에서 처리해도 됩니다)
        // private final BoardService boardService; 추가 필요

        // 2. 전체 게시글 수 및 페이지네이션 계산
        int totalPostNum = postService.selectPostListNumByBoardCd(bbsClsfCd); // 검색 기능 적용 시 이 메소드 수정 필요
        int totalPage = (totalPostNum > 0) ? (int) Math.ceil((double) totalPostNum / size) : 1;
        int pageNavSize = 10;
        int startPageNum = ((page - 1) / pageNavSize) * pageNavSize + 1;
        int endPageNum = Math.min(startPageNum + pageNavSize - 1, totalPage);
        int offset = (page - 1) * size;

        // 3. 현재 페이지의 게시글 목록 조회
        List<Post> postList = postService.selectPostListByBoardCd(bbsClsfCd, offset, size); // 검색 기능 적용 시 이 메소드 수정 필요

        // 4. 프론트엔드로 보낼 모든 데이터를 Map에 담기
        Map<String, Object> response = new HashMap<>();
        response.put("postList", postList);
        response.put("currentPage", page);
        response.put("size", size);
        response.put("bbsClsfCd", bbsClsfCd);
        response.put("totalPage", totalPage);
        response.put("startPageNum", startPageNum);
        response.put("endPageNum", endPageNum);
        // boardName도 필요 시 추가

        return ResponseEntity.ok(response);
    }
}
