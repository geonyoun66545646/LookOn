package ks55team02.customer.post.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import ks55team02.customer.login.domain.LoginUser; // [수정] LoginUser 임포트
import ks55team02.customer.post.domain.Board;
import ks55team02.customer.post.domain.Post;
import ks55team02.customer.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/customer/post")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    @GetMapping("/postList")
    public String selectPostList(
            @RequestParam(required = false) String bbsClsfCd,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model) {
        List<Board> boardList = postService.selectBoardName();
        model.addAttribute("boardList", boardList);
        model.addAttribute("bbsClsfCd", bbsClsfCd);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        return "customer/post/postList";
    }

    @GetMapping("/postDetail")
    public String selectPostDetail(@RequestParam String pstSn, Model model, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        String loginUserNo = (loginUser != null) ? loginUser.getUserNo() : null;
        String loginUserGrade = (loginUser != null) ? loginUser.getMbrGrdCd() : null;

        Post postDetail = postService.selectPostDetailByPostSn(pstSn, loginUserNo);
        
        if (postDetail != null) {
            postService.increaseViewCount(pstSn);

            // 댓글 작성 가능 여부 판단 로직 추가
            boolean isAdmin = "grd_cd_0".equals(loginUserGrade) || "grd_cd_1".equals(loginUserGrade);
            boolean canComment = loginUser != null && (isAdmin || !"admin_only".equals(postDetail.getCmntWrtAuthrtLvlVal()));
            model.addAttribute("canComment", canComment);
        }

        model.addAttribute("postDetail", postDetail);
        model.addAttribute("loginUser", loginUser);
        return "customer/post/postDetail";
    }

    @GetMapping("/postWrite")
    public String postWriteForm(Model model, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        List<Board> boardList = postService.selectBoardListForWrite();

        // 관리자가 아닌 경우 'admin_only' 게시판 필터링
        if (loginUser == null || (!"grd_cd_0".equals(loginUser.getMbrGrdCd()) && !"grd_cd_1".equals(loginUser.getMbrGrdCd()))) {
            boardList = boardList.stream()
                                 .filter(b -> !"admin_only".equals(b.getWrtAuthrtLvlVal()))
                                 .collect(Collectors.toList());
        }

        model.addAttribute("boardList", boardList);
        model.addAttribute("post", new Post());
        return "customer/post/postWrite";
    }

    @GetMapping("/postUpdate/{pstSn}")
    public String postUpdateForm(@PathVariable String pstSn, Model model, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        String loginUserNo = (loginUser != null) ? loginUser.getUserNo() : null;

        Post postToUpdate = postService.selectPostDetailByPostSn(pstSn, loginUserNo);
        List<Board> boardList = postService.selectBoardListForWrite();

        // 관리자가 아닌 경우 'admin_only' 게시판 필터링
        if (loginUser == null || (!"grd_cd_0".equals(loginUser.getMbrGrdCd()) && !"grd_cd_1".equals(loginUser.getMbrGrdCd()))) {
            boardList = boardList.stream()
                                 .filter(b -> !"admin_only".equals(b.getWrtAuthrtLvlVal()))
                                 .collect(Collectors.toList());
        }
        
        model.addAttribute("boardList", boardList);
        model.addAttribute("post", postToUpdate);
        return "customer/post/postWrite";
    }
}