package ks55team02.customer.post.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ks55team02.customer.post.domain.Post;
import ks55team02.customer.post.service.PostService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/customer/post")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;
	
	@DeleteMapping("/postDelete/{pstSn}")
	@ResponseBody
	public String postDelete(@PathVariable String pstSn) {
	    try {
	        postService.deletePost(pstSn);
	        // 이 "삭제 성공" 이라는 문자열이 그대로 Ajax의 success 콜백으로 전달됩니다.
	        return "삭제 성공";
	    } catch (Exception e) {
	        // 이 부분은 약간의 함정이 있습니다. 아래 설명 참고.
	        // 일단 실패 시에도 문자열을 반환합니다.
	        return null;
	    }
	}

	@GetMapping("/postWrite")
	public String postWrite(
	        @RequestParam(required = false) String pstSn,
	        Model model) {
	    Post post = new Post();
	    if (pstSn != null && !pstSn.isEmpty()) {
	        post = postService.selectPostDetailByPostSn(pstSn); // 여기서 post 객체가 제대로 채워져야 함
	    }
	    // Model에 'post'라는 이름으로 Post 객체를 담는 것이 필수입니다.
	    model.addAttribute("post", post);
	    // ...
	    return "customer/post/postWrite";
	}
	
	// 게시글 작성/수정 POST 요청 처리 (AJAX 또는 일반 폼 제출)
	@PostMapping("/postWrite")
	public String submitPost(
			Post post, // 폼 데이터가 Post 객체로 자동 바인딩됩니다.
			Model model) {
		
		// 여기서 post 객체에는 사용자가 입력한 title, content, category (bbsClsfCd) 등이 담겨 옵니다.
		// 만약 수정 모드라면, post.getPstSn() 값도 있을 것입니다.
		
		// Post 클래스에 category 필드가 없으므로, HTML의 name="category"로 받은 값을
		// Controller에서 Post 객체의 bbsClsfCd 필드로 설정해주는 로직이 필요합니다.
		// 하지만 지금은 Post post 파라미터만 받으므로,
		// html의 select name을 bbsClsfCd로 변경해야 이 바인딩이 직접적으로 이루어집니다.
		// 즉, HTML의 <select id="postCategory" name="bbsClsfCd" ...> 와 같이 되어야 합니다.

		// 작성 또는 수정 로직 수행 (postService를 통해)
		if (post.getPstSn() == null || post.getPstSn().isEmpty()) {
			// 새 글 작성
			postService.insertPost(post);
		}

		// 저장 후 목록 페이지로 리다이렉트
		return "redirect:/customer/post/postList";
	}
	
	

	// 게시글 조회
	@GetMapping("/postView")
	public String postView(@RequestParam String pstSn, Model model) {

		Post postDetail = postService.selectPostDetailByPostSn(pstSn);

		model.addAttribute("postDetail", postDetail);

		return "customer/post/postView";
	}

	// 게시글 목록 조회
	@GetMapping("/postList")
	public String postListView(
			@RequestParam(required = false) String bbsClsfCd,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "#{paginationProperties.defaultSize}") int size,
			Model model) {

		int totalPostNum = postService.selectPostListNumByBoardCd(bbsClsfCd);
		int totalPage = 0;
		if(totalPostNum > 0) {
			totalPage = (int) Math.ceil((double) totalPostNum / size);
		} else {
			totalPage = 1;
		}

        System.out.println("### 게시글 총 개수 (totalPostNum): " + totalPostNum);
        System.out.println("### 현재 페이지 (currentPage): " + page);
        System.out.println("### 페이지당 게시글 수 (size): " + size);
        System.out.println("### 총 페이지 수 (totalPage): " + totalPage);

		int offset = (page - 1) * size;

		var postList = postService.selectPostListByBoardCd(bbsClsfCd, offset, size);

		model.addAttribute("postList", postList);
		model.addAttribute("currentPage", page);
		model.addAttribute("size", size);
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("bbsClsfCd", bbsClsfCd);

		return "customer/post/postList";
	}
}
