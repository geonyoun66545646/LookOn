// AdminSearchService.java
package ks55team02.admin.adminpage.useradmin.search.service;

import ks55team02.admin.adminpage.useradmin.search.domain.AdminSearchStats;
import java.util.List;
import java.util.Map; // Map import 추가

public interface AdminSearchService {

	/**
	 * 키워드별 검색 통계 조회 (페이지네이션 및 검색 조건 적용)
	 * 
	 * @param currentPage  현재 페이지 번호
	 * @param searchParams 검색 조건 (searchValue, searchStartDate, searchEndDate)
	 * @return 검색 통계 리스트와 페이지네이션 정보가 담긴 Map
	 */
	// ✅ [수정] Map<String, Object> searchParams 파라미터를 추가합니다.
	Map<String, Object> getSearchStats(int currentPage, Map<String, Object> searchParams);

	/**
	 * 검색 로그 목록 조회 (페이지네이션 및 검색 조건 적용)
	 * 
	 * @param currentPage  현재 페이지 번호
	 * @param searchParams 검색 조건
	 * @return 검색 로그 리스트와 페이지네이션 정보가 담긴 Map
	 */
	Map<String, Object> getSearchLogList(int currentPage, Map<String, Object> searchParams);
}