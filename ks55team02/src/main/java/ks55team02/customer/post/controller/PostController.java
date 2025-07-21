package ks55team02.customer.post.controller;

import ks55team02.customer.login.domain.LoginUser; // [수정] LoginUser 임포트
import ks55team02.customer.post.domain.Board;
import ks55team02.customer.post.domain.Post;
import ks55team02.customer.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import java.util.List;

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

    @GetMapping("/postView")
    public String selectPostDetail(@RequestParam String pstSn, Model model, HttpSession session) {
        // [수정] 세션에서 실제 타입인 LoginUser로 객체를 가져옵니다.
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        String loginUserNo = (loginUser != null) ? loginUser.getUserNo() : null;

        Post postDetail = postService.selectPostDetailByPostSn(pstSn, loginUserNo);
        
        if (postDetail != null) {
            postService.increaseViewCount(pstSn);
        }

        model.addAttribute("postDetail", postDetail);
        // [수정] LoginUser 객체 전체를 뷰로 전달합니다.
        model.addAttribute("loginUser", loginUser);
        return "customer/post/postView";
    }

    @GetMapping("/postWrite")
    public String postWriteForm(Model model) {
        List<Board> boardList = postService.selectBoardName();
        model.addAttribute("boardList", boardList);
        model.addAttribute("post", new Post());
        return "customer/post/postWrite";
    }

    @GetMapping("/postUpdate/{pstSn}")
    public String postUpdateForm(@PathVariable String pstSn, Model model, HttpSession session) {
        // [수정] 세션 처리 로직을 selectPostDetail과 동일하게 맞춥니다.
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        String loginUserNo = (loginUser != null) ? loginUser.getUserNo() : null;

        Post postToUpdate = postService.selectPostDetailByPostSn(pstSn, loginUserNo);
        List<Board> boardList = postService.selectBoardName();
        
        model.addAttribute("boardList", boardList);
        model.addAttribute("post", postToUpdate);
        return "customer/post/postWrite";
    }
}