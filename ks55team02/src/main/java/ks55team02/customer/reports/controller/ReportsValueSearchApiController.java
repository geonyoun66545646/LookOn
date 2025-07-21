package ks55team02.customer.reports.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ks55team02.customer.reports.service.ReportsValueSearchService;

@RestController
@RequestMapping("/api/reports-value-search")
public class ReportsValueSearchApiController {

	private final ReportsValueSearchService reportsValueSearchService;

	public ReportsValueSearchApiController(ReportsValueSearchService reportsValueSearchService) {
		this.reportsValueSearchService = reportsValueSearchService;
	}

	@GetMapping("/posts")
	public ResponseEntity<List<Map<String, Object>>> searchPosts(@RequestParam String keyword) {
		return ResponseEntity.ok(reportsValueSearchService.searchPosts(keyword));
	}

	@GetMapping("/comments")
	public ResponseEntity<List<Map<String, Object>>> searchComments(@RequestParam String keyword) {
		return ResponseEntity.ok(reportsValueSearchService.searchComments(keyword));
	}

	@GetMapping("/products")
	public ResponseEntity<List<Map<String, Object>>> searchProducts(@RequestParam String keyword) {
		return ResponseEntity.ok(reportsValueSearchService.searchProducts(keyword));
	}

	@GetMapping("/users")
	public ResponseEntity<List<Map<String, Object>>> searchUsers(@RequestParam String keyword) {
		return ResponseEntity.ok(reportsValueSearchService.searchUsers(keyword));
	}
}