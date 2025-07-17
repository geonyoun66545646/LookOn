// AdminSearchServiceImpl.java (최종 수정본)
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

    private static final int PAGE_SIZE = 10;
    private static final int PAGE_BLOCK_SIZE = 5;

    @Override
    public Map<String, Object> getSearchStats(int currentPage, Map<String, Object> searchParams) {
        int totalRecordCount = adminSearchMapper.getSearchStatsCount(searchParams);

        // --- 페이지네이션 계산 ---
        int totalPageCount = (int) Math.ceil((double) totalRecordCount / PAGE_SIZE);
        int startPage = ((currentPage - 1) / PAGE_BLOCK_SIZE) * PAGE_BLOCK_SIZE + 1;
        int endPage = Math.min(startPage + PAGE_BLOCK_SIZE - 1, totalPageCount);
        int limitStart = (currentPage - 1) * PAGE_SIZE;

        // --- DB 조회용 파라미터 설정 ---
        Map<String, Object> paramsForDb = new HashMap<>(searchParams);
        paramsForDb.put("start", limitStart);
        paramsForDb.put("size", PAGE_SIZE);
        List<AdminSearchStats> statsList = adminSearchMapper.getSearchStats(paramsForDb);

        // ✅ [핵심 수정 1] 프론트엔드가 기대하는 'pagination' 객체를 만듭니다.
        Map<String, Object> paginationMap = new HashMap<>();
        paginationMap.put("currentPage", currentPage);
        paginationMap.put("totalRecordCount", totalRecordCount);
        paginationMap.put("lastPage", totalPageCount); // JS에서 lastPage를 사용할 수 있으므로 추가
        paginationMap.put("startPage", startPage);
        paginationMap.put("endPage", endPage);
        paginationMap.put("existPrevBlock", startPage > 1);
        paginationMap.put("existNextBlock", endPage < totalPageCount);

        // ✅ [핵심 수정 2] 최종 결과 Map에 'pagination' 객체를 통째로 넣습니다.
        Map<String, Object> result = new HashMap<>();
        result.put("statsList", statsList);
        result.put("pagination", paginationMap);

        return result;
    }

    @Override
    public Map<String, Object> getSearchLogList(int currentPage, Map<String, Object> searchParams) {
        int totalRecordCount = adminSearchMapper.getSearchLogListCount(searchParams);

        // --- 페이지네이션 계산 ---
        int totalPageCount = (int) Math.ceil((double) totalRecordCount / PAGE_SIZE);
        int startPage = ((currentPage - 1) / PAGE_BLOCK_SIZE) * PAGE_BLOCK_SIZE + 1;
        int endPage = Math.min(startPage + PAGE_BLOCK_SIZE - 1, totalPageCount);
        int limitStart = (currentPage - 1) * PAGE_SIZE;

        // --- DB 조회용 파라미터 설정 ---
        searchParams.put("start", limitStart);
        searchParams.put("size", PAGE_SIZE);
        List<AdminSearchLog> logList = adminSearchMapper.getSearchLogList(searchParams);

        // ✅ [핵심 수정 1] 프론트엔드가 기대하는 'pagination' 객체를 만듭니다.
        Map<String, Object> paginationMap = new HashMap<>();
        paginationMap.put("currentPage", currentPage);
        paginationMap.put("totalRecordCount", totalRecordCount);
        paginationMap.put("lastPage", totalPageCount);
        paginationMap.put("startPage", startPage);
        paginationMap.put("endPage", endPage);
        paginationMap.put("existPrevBlock", startPage > 1);
        paginationMap.put("existNextBlock", endPage < totalPageCount);
        
        // ✅ [핵심 수정 2] 최종 결과 Map에 'pagination' 객체를 통째로 넣습니다.
        Map<String, Object> result = new HashMap<>();
        result.put("logList", logList);
        result.put("pagination", paginationMap);

        return result;
    }
}
