package ks55team02.customer.post.controller;

import java.util.List;

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
import ks55team02.customer.post.domain.Comment;
import ks55team02.customer.post.domain.Interaction;
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
	
	// 추천수 증가
	@PostMapping("/interactionInsert")
	@ResponseBody
	public void insertInteraction(@PathVariable Interaction interaction, Model model) {
		try {
			postService.insertInterCount(interaction);
		} catch (Exception e) {
		}
	}
	
	
	// 댓글 삭제
	@DeleteMapping("/commentDelete/{pstCmntSn}")
	@ResponseBody
	public String deleteComment(@PathVariable String pstCmntSn) {
		try {
			postService.deleteComment(pstCmntSn);
			return "삭제 성공";
		} catch (Exception e) {
			return null;			
		}
	}

	
	// 게시글 삭제
	@DeleteMapping("/postDelete/{pstSn}")
	@ResponseBody
	public String deletePost(@PathVariable String pstSn) {
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

	// 게시글 수정
	@PostMapping("/postUpdate")
	@ResponseBody // Ajax 요청에 '데이터'를 직접 응답하기 위한 필수 어노테이션
	public String updatePost(Post post) { // 수정된 폼 데이터가 자동으로 Post 객체에 담깁니다.
	    
	    // try-catch로 예외 상황을 처리하는 것이 안전합니다.
	    try {
	        // "수정" 서비스 메소드를 호출합니다.
	        postService.updatePost(post);
	        
	        // 성공했을 때, Ajax의 .done() 부분으로 전달될 성공 메시지입니다.
	        return "수정 성공";
	        
	    } catch (Exception e) {
	        
	        // 만약 Service나 Mapper에서 오류가 발생하면, 콘솔에 에러 로그를 출력합니다.
	        // 이렇게 하면 디버깅할 때 원인을 찾기 매우 쉽습니다.
	        log.error("글 수정 처리 중 오류 발생: {}", e.getMessage());
	        e.printStackTrace(); // 전체 에러 스택을 출력해서 더 자세히 볼 수 있습니다.
	        
	        // 실패했을 때, Ajax의 .fail() 부분으로 전달될 실패 메시지입니다.
	        // 이 경우, Ajax는 성공으로 착각할 수 있으므로 상태 코드를 보내주는 것이 더 좋지만,
	        // 일단은 이렇게 문자열로 구분하는 것도 가능합니다.
	        return "수정 실패";
	    }
	}	
	
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
	
	// 게시글 작성 POST 요청 처리 (AJAX 또는 일반 폼 제출)
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

	// 게시글 목록 조회
	@GetMapping("/postList")
	public String selectPostList(
			@RequestParam(required = false) String bbsClsfCd,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "#{paginationProperties.defaultSize}") int size,
			Model model) {

		List<Board> boardList = boardService.selectBoardName();
		String boardName = "전체게시판";
		if(bbsClsfCd != null) {
			for(Board b : boardList) {
				if(bbsClsfCd.equals(b.getBbsClsfCd())) {
					boardName = b.getBbsNm();
				}
			}
		}
		
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

		model.addAttribute("boardList", boardList);
		model.addAttribute("boardName", boardName);
		model.addAttribute("postList", postList);
		model.addAttribute("currentPage", page);
		model.addAttribute("size", size);
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("bbsClsfCd", bbsClsfCd);

		return "customer/post/postList";
	}
}
