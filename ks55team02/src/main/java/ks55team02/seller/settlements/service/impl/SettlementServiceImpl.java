package ks55team02.seller.settlements.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ks55team02.admin.common.domain.Pagination;
import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.seller.settlements.domain.SalesSttsDTO;
import ks55team02.seller.settlements.domain.SettlementDTO;
import ks55team02.seller.settlements.domain.SettlementSearchCriteria;
import ks55team02.seller.settlements.mapper.SettlementMapper;
import ks55team02.seller.settlements.service.SettlementService;

@Service
public class SettlementServiceImpl implements SettlementService {
	private final SettlementMapper settlementMapper;

    public SettlementServiceImpl(SettlementMapper settlementMapper) {
        this.settlementMapper = settlementMapper;
    }

    @Override
    public Map<String, Object> getMySettlementList(SearchCriteria criteria) {
        // 이 안의 코드는 이제 오류가 없어야 합니다.
        int totalRecordCount = settlementMapper.countMySettlementList(criteria);
        Pagination pagination = new Pagination(totalRecordCount, criteria);
        criteria.setOffset(pagination.getLimitStart());
        criteria.setPageSize(pagination.getRecordSize());
        List<SettlementDTO> settlementList = settlementMapper.selectMySettlementList(criteria);

        Map<String, Object> result = new HashMap<>();
        result.put("settlementList", settlementList);
        result.put("pagination", pagination);
        return result;
    }


    @Override
    public String getStoreIdByUserNo(String userNo) {
        return settlementMapper.selectStoreIdByUserNo(userNo);
    }

    /**
     * 특정 상점의 판매 내역 조회 구현
     * 이 메서드는 컨트롤러의 /api/{storeId} 엔드포인트에서 사용됩니다.
     * 여기서는 페이지네이션 없이 해당 상점의 모든 판매 내역을 가져옵니다.
     * 만약 이 메서드도 페이지네이션을 적용해야 한다면, 컨트롤러의 해당 API도 변경해야 합니다.
     */
    @Override
    public List<SalesSttsDTO> getSalesHistoryByStoreId(String storeId) {
        return settlementMapper.getSalesHistoryByStoreId(storeId);
    }

    @Override
    public List<SalesSttsDTO> getAllSalesHistory() {
        return settlementMapper.getAllSalesHistory();
    }

    /**
     * 특정 상점의 판매 내역 개수 조회 구현
     */
    @Override
    public int getSalesHistoryCountByStoreId(SettlementSearchCriteria criteria) {
        return settlementMapper.countSalesHistoryByStoreId(criteria);
    }

    /**
     * 페이지네이션 적용된 특정 상점의 판매 내역 조회 구현
     */
    @Override
    public List<SalesSttsDTO> getSalesHistoryByStoreIdAndPagination(SettlementSearchCriteria criteria) {
        return settlementMapper.selectSalesHistoryByStoreIdAndPagination(criteria);
    }
}