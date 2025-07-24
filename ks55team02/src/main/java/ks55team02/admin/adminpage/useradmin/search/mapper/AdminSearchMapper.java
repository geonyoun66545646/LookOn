package ks55team02.admin.adminpage.useradmin.search.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param; // @Param import 추가

import ks55team02.admin.adminpage.useradmin.search.domain.AdminSearchLog;
import ks55team02.admin.adminpage.useradmin.search.domain.AdminSearchStats;

@Mapper
public interface AdminSearchMapper {

	/**
	 * 키워드별 검색 통계를 조회 (페이지네이션 적용)
	 * @param params 페이지네이션 정보 (start, size)와 검색 조건
	 * @return 키워드, 검색횟수가 담긴 AdminSearchStats 객체 리스트
	 */
	// ▼▼▼ 모든 메소드의 파라미터 앞에 @Param("searchParams")를 추가합니다. ▼▼▼
	List<AdminSearchStats> getSearchStats(@Param("searchParams") Map<String, Object> params);

	/**
	 * 키워드 통계의 전체 개수 (전체 페이지 수를 계산하기 위함)
	 * @param params 검색 조건
	 * @return 그룹화된 키워드의 총 개수
	 */
	int getSearchStatsCount(@Param("searchParams") Map<String, Object> params);
	 
	/**
	 * 검색 조건에 맞는 검색 로그 목록을 조회 (페이지네이션 적용)
	 * @param params 검색 조건과 페이지네이션 정보가 담긴 Map
	 * @return 검색 로그 리스트
	 */
	List<AdminSearchLog> getSearchLogList(@Param("searchParams") Map<String, Object> params);

	/**
	 * 검색 조건에 맞는 검색 로그의 총 개수를 조회
	 * @param params 검색 조건 정보가 담긴 Map
	 * @return 로그 총 개수
	 */
	int getSearchLogListCount(@Param("searchParams") Map<String, Object> params);
	
	

}