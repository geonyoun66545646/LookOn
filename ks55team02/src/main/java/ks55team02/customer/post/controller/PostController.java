package ks55team02.customer.post.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ks55team02.customer.post.domain.Board;
import ks55team02.customer.post.domain.PostInteraction;
import ks55team02.customer.post.domain.Post;
import ks55team02.customer.post.service.BoardService;
import ks55team02.customer.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/customer/post")
@RequiredArgsConstructor
@Slf4j
public class PostController {

	private final PostService postService;
	private final BoardService boardService;
	

	// 게시글 수정 정보
	@GetMapping("/postUpdate/{pstSn}")
	public String selectPostUpdateForm(@PathVariable String pstSn, Model model) {
	    // 1. pstSn을 사용해서 DB에서 수정할 게시글의 정보를 가져옵니다.
		List<Board> boardList = boardService.selectBoardName();
	    Post postToUpdate = postService.selectPostDetailByPostSn(pstSn);
	    
	    // 2. 가져온 게시글 정보를 Model에 담아서 뷰로 전달합니다.
		model.addAttribute("boardList", boardList);
	    model.addAttribute("post", postToUpdate);
	    
	    // 3. '글 작성'에 사용했던 그 폼 페이지를 그대로 재사용합니다.
	    return "customer/post/postWrite"; 
	}
	
	// 게시글 작성 정보
	@PostMapping("/postWrite")
	public String submitPost(
			Post post, // 폼 데이터가 Post 객체로 자동 바인딩됩니다.
			Model model) {

		// 새 글 작성
		postService.insertPost(post);


		// 저장 후 목록 페이지로 리다이렉트
		return "redirect:/customer/post/postList";
	}

	
	// 게시글 작성
	@GetMapping("/postWrite")
	public String postWrite(
	        @RequestParam(required = false) String pstSn,
	        Model model) {
		List<Board> boardList = boardService.selectBoardName();
	    Post post = new Post();
	    if(pstSn != null && !pstSn.isEmpty()) {
	        post = postService.selectPostDetailByPostSn(pstSn); // 여기서 post 객체가 제대로 채워져야 함
	    }
	    // Model에 'post'라는 이름으로 Post 객체를 담는 것이 필수입니다.
	    model.addAttribute("boardList", boardList);
	    model.addAttribute("post", post);
	    // ...
	    return "customer/post/postWrite";
	}


	// 게시글 조회
	@GetMapping("/postView")
	public String selectPostDetail(@RequestParam String pstSn, Model model) {

		Post postDetail = postService.selectPostDetailByPostSn(pstSn);

		model.addAttribute("postDetail", postDetail);

		return "customer/post/postView";
	}

    // 게시글 목록 조회 (역할이 축소된 최종 버전)
    @GetMapping("/postList")
    public String selectPostList(
            @RequestParam(required = false) String bbsClsfCd,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size, // JS와 일치하도록 기본값 10으로 수정
            @RequestParam(required = false) String keyword,
            Model model) {

        // 1. 페이지 '틀'을 구성하는 데 필요한 데이터만 전달
        List<Board> boardList = boardService.selectBoardName();
        String boardName = "전체게시판";
		if(bbsClsfCd != null) {
			for(Board b : boardList) {
				if(bbsClsfCd.equals(b.getBbsClsfCd())) {
					boardName = b.getBbsNm();
				}
			}
		}

        model.addAttribute("boardList", boardList);
        model.addAttribute("boardName", boardName);

        // 2. JavaScript가 초기 상태를 알 수 있도록 파라미터를 그대로 전달 (data-* 속성용)
        model.addAttribute("bbsClsfCd", bbsClsfCd);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);

        // ※※※ postList, startPageNum, endPageNum 등은 이 메소드에서 완전히 제거되었습니다. ※※※
        return "customer/post/postList";
    }
}
