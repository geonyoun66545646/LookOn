// AdminSearchServiceImpl.java
package ks55team02.admin.adminpage.useradmin.search.service.impl;

import ks55team02.admin.adminpage.useradmin.search.domain.AdminSearchLog;
import ks55team02.admin.adminpage.useradmin.search.domain.AdminSearchStats;
import ks55team02.admin.adminpage.useradmin.search.mapper.AdminSearchMapper;
import ks55team02.admin.adminpage.useradmin.search.service.AdminSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminSearchServiceImpl implements AdminSearchService {

    private final AdminSearchMapper adminSearchMapper;
    
    // 페이지네이션 상수 정의
    private static final int PAGE_SIZE = 10; // 한 페이지에 보여줄 아이템 개수
    private static final int PAGE_BLOCK_SIZE = 5; // 한 번에 보여줄 페이지 번호 개수 (예: 1 2 3 4 5)

    /**
     * 키워드별 검색 통계 조회 (페이지네이션 적용)
     */
    @Override
    public Map<String, Object> getSearchStats(int currentPage) {
        // 1. 전체 통계 개수 조회
        int totalStatsCount = adminSearchMapper.getSearchStatsCount();
        

        // 2. 페이지네이션 계산
        int startRow = (currentPage - 1) * PAGE_SIZE;
        int lastPage = (int) Math.ceil((double) totalStatsCount / PAGE_SIZE);
        int startPage = ((currentPage - 1) / PAGE_BLOCK_SIZE) * PAGE_BLOCK_SIZE + 1;
        int endPage = Math.min(startPage + PAGE_BLOCK_SIZE - 1, lastPage);

        // 3. Mapper에 전달할 파라미터 설정
        Map<String, Object> params = new HashMap<>();
        params.put("start", startRow);
        params.put("size", PAGE_SIZE);

        // 4. 통계 목록 조회
        List<AdminSearchStats> statsList = adminSearchMapper.getSearchStats(params);

        // 5. 컨트롤러에 전달할 결과 Map 생성
        Map<String, Object> result = new HashMap<>();
        result.put("statsList", statsList);
        result.put("lastPage", lastPage);
        result.put("startPage", startPage);
        result.put("endPage", endPage);
        result.put("currentPage", currentPage);
        result.put("totalStatsCount", totalStatsCount);

        return result;
    }

    /**
     * 검색 로그 목록 조회 (페이지네이션 및 검색 조건 적용)
     */
    @Override
    public Map<String, Object> getSearchLogList(int currentPage, Map<String, Object> searchParams) {
        // 1. 조건에 맞는 전체 로그 개수 조회
        int totalLogCount = adminSearchMapper.getSearchLogListCount(searchParams);

        // 2. 페이지네이션 계산
        int startRow = (currentPage - 1) * PAGE_SIZE;
        int lastPage = (int) Math.ceil((double) totalLogCount / PAGE_SIZE);
        int startPage = ((currentPage - 1) / PAGE_BLOCK_SIZE) * PAGE_BLOCK_SIZE + 1;
        int endPage = Math.min(startPage + PAGE_BLOCK_SIZE - 1, lastPage);

        // 3. Mapper에 전달할 파라미터 설정 (기존 검색 조건 + 페이지네이션)
        searchParams.put("start", startRow);
        searchParams.put("size", PAGE_SIZE);

        // 4. 로그 목록 조회
        List<AdminSearchLog> logList = adminSearchMapper.getSearchLogList(searchParams);

        // 5. 컨트롤러에 전달할 결과 Map 생성
        Map<String, Object> result = new HashMap<>();
        result.put("logList", logList);
        result.put("lastPage", lastPage);
        result.put("startPage", startPage);
        result.put("endPage", endPage);
        result.put("currentPage", currentPage);
        result.put("totalLogCount", totalLogCount);

        return result;
    }
}