package ks55team02.admin.adminpage.useradmin.search.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ks55team02.admin.adminpage.useradmin.search.service.AdminSearchService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/search")
@RequiredArgsConstructor
public class AdminSearchApiController {

	private final AdminSearchService adminSearchService;

	/**
	 * 검색 통계 데이터를 조회하는 API (페이지네이션 및 검색 조건 적용)
	 * 
	 * @param currentPage  요청된 페이지 번호 (기본값 1)
	 * @param searchParams 검색 조건들(searchValue, searchStartDate, searchEndDate)을 담는
	 *                     Map
	 * @return JSON 형태의 검색 통계 리스트와 페이지네이션 정보
	 */
	@GetMapping("/stats")
	// ✅ [수정] getSearchLogList와 동일하게, @RequestParam Map<String, Object>
	// searchParams를 추가합니다.
	public ResponseEntity<Map<String, Object>> getSearchStats(
			@RequestParam(value = "page", defaultValue = "1") int currentPage,
			@RequestParam Map<String, Object> searchParams) {

		// ✅ 서비스 계층에 currentPage와 함께 searchParams를 그대로 전달합니다.
		Map<String, Object> responseData = adminSearchService.getSearchStats(currentPage, searchParams);
		return ResponseEntity.ok(responseData);
	}

	/**
	 * 검색 로그 데이터를 조회하는 API (페이지네이션 및 검색 조건 적용) 이 메소드는 이미 올바르게 작성되어 있으므로 수정할 필요가
	 * 없습니다.
	 */
	@GetMapping("/logs")
	public ResponseEntity<Map<String, Object>> getSearchLogList(
			@RequestParam(value = "page", defaultValue = "1") int currentPage,
			@RequestParam Map<String, Object> searchParams) {

		Map<String, Object> responseData = adminSearchService.getSearchLogList(currentPage, searchParams);
		return ResponseEntity.ok(responseData);
	}
}