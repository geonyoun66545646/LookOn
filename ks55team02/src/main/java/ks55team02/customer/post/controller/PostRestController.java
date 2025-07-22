package ks55team02.customer.post.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.customer.post.domain.Post;
import ks55team02.customer.post.domain.PostComment;
import ks55team02.customer.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/customer/api/post")
@RequiredArgsConstructor
@Slf4j
public class PostRestController {

    private final PostService postService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getPostList(
            @RequestParam(required = false) String bbsClsfCd,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size) {
        try {
            int totalPostNum = postService.selectPostListNumByBoardCd(bbsClsfCd);
            int offset = (page - 1) * size;
            boolean hasNext = (page * size) < totalPostNum;
            List<Post> postList = postService.selectPostListByBoardCd(bbsClsfCd, offset, size);
            Map<String, Object> response = new HashMap<>();
            response.put("postList", postList);
            response.put("hasNext", hasNext);
            response.put("totalCount", totalPostNum);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching post list data: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("postList", Collections.emptyList(), "hasNext", false));
        }
    }

    @PostMapping(value = "/write", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> writePost(
            @RequestParam("bbsClsfCd") String bbsClsfCd,
            @RequestParam("pstTtl") String pstTtl,
            @RequestParam("pstCn") String pstCn,
            @RequestParam(value = "newImageFiles", required = false) List<MultipartFile> imageFiles,
            HttpSession session) {
        
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Post post = new Post();
            post.setBbsClsfCd(bbsClsfCd);
            post.setPstTtl(pstTtl);
            post.setPstCn(pstCn);
            post.setWrtrUserNo(loginUser.getUserNo());
            
            postService.insertPost(post, imageFiles);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("result", "success", "message", "게시글이 성공적으로 작성되었습니다."));
        } catch (Exception e) {
            log.error("게시글 작성 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("result", "fail", "message", "게시글 작성 중 오류가 발생했습니다."));
        }
    }

    @PostMapping(value = "/update/{pstSn}", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> updatePost(
            @PathVariable("pstSn") String pstSn,
            @RequestParam("bbsClsfCd") String bbsClsfCd,
            @RequestParam("pstTtl") String pstTtl,
            @RequestParam("pstCn") String pstCn,
            @RequestParam(value = "deleteImageSns", required = false) List<String> deleteImageSns,
            @RequestParam(value = "newImageFiles", required = false) List<MultipartFile> newImageFiles,
            HttpSession session) {

        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Post post = new Post();
            post.setPstSn(pstSn);
            post.setBbsClsfCd(bbsClsfCd);
            post.setPstTtl(pstTtl);
            post.setPstCn(pstCn);
            post.setWrtrUserNo(loginUser.getUserNo());

            boolean isSuccess = postService.updatePost(post, newImageFiles, deleteImageSns, loginUser.getUserNo());

            if (isSuccess) {
                return ResponseEntity.ok(Map.of("result", "success", "message", "게시글이 성공적으로 수정되었습니다."));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("result", "fail", "message", "게시글을 수정할 권한이 없습니다."));
            }
        } catch (Exception e) {
            log.error("게시글 수정 중 오류 발생: pstSn={}, {}", pstSn, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("result", "fail", "message", "게시글 수정 중 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/delete/{pstSn}")
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable String pstSn, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Map<String, Object> response = new HashMap<>();
        try {
            postService.deletePost(pstSn);
            response.put("result", "success");
            response.put("message", "게시글이 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("result", "fail", "message", "게시글 삭제 중 오류 발생"));
        }
    }
    
    @PostMapping("/toggle-like")
    public ResponseEntity<Map<String, Object>> toggleLike(@RequestBody Map<String, String> payload, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String pstSn = payload.get("pstSn");
        String loginUserNo = loginUser.getUserNo();

        try {
            boolean success = postService.addPostLike(pstSn, loginUserNo);

            if (success) {
                int currentLikeCount = postService.countInteractionsByPost(pstSn);
                Map<String, Object> response = new HashMap<>();
                response.put("isLiked", true);
                response.put("likeCount", currentLikeCount);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } catch (Exception e) {
            log.error("Error toggling like for postSn={}: {}", pstSn, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "처리 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/comments")
    public ResponseEntity<Map<String, Object>> writeComment(@RequestBody PostComment comment, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        comment.setWrtrUserNo(loginUser.getUserNo());
        
        Map<String, Object> response = new HashMap<>();
        try {
            postService.insertComment(comment);
            response.put("result", "success");
            response.put("message", "댓글이 등록되었습니다.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("result", "fail", "message", "댓글 등록 중 오류 발생"));
        }
    }

    @PutMapping("/comments/{pstCmntSn}")
    public ResponseEntity<Map<String, Object>> updateComment(@PathVariable String pstCmntSn, @RequestBody PostComment comment, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Map<String, Object> response = new HashMap<>();
        try {
            comment.setPstCmntSn(pstCmntSn);
            postService.updateComment(comment);
            response.put("result", "success");
            response.put("message", "댓글이 수정되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("result", "fail", "message", "댓글 수정 중 오류 발생"));
        }
    }

    @DeleteMapping("/comments/{pstCmntSn}")
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable String pstCmntSn, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Map<String, Object> response = new HashMap<>();
        try {
            postService.deleteComment(pstCmntSn);
            response.put("result", "success");
            response.put("message", "댓글이 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("result", "fail", "message", "댓글 삭제 중 오류 발생"));
        }
    }
}