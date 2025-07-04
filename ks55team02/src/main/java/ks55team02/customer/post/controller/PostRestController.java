package ks55team02.customer.post.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ks55team02.customer.post.domain.Comment;
import ks55team02.customer.post.service.PostService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/customer/post")
@Slf4j
public class PostRestController {

    @Autowired
    private PostService postService;
    
    // 댓글 수정
    @PostMapping("/updateComment")
    public Map<String, Object> updateComment(Comment comment) {
    	
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
	public Map<String, Object> insertComment(Comment comment) {
		
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
	
}
