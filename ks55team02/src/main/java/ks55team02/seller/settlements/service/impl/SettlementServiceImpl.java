package ks55team02.seller.settlements.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ks55team02.seller.settlements.domain.SalesSttsDTO;
import ks55team02.seller.settlements.domain.SettlementDTO;
import ks55team02.seller.settlements.domain.SettlementSearchCriteria;
import ks55team02.seller.settlements.mapper.SettlementMapper;
import ks55team02.seller.settlements.service.SettlementService;
import ks55team02.util.CustomerPagination;

@Service
public class SettlementServiceImpl implements SettlementService{
	private final SettlementMapper settlementMapper;

    public SettlementServiceImpl(SettlementMapper settlementMapper) {
        this.settlementMapper = settlementMapper;
    }

    @Override
    public Map<String, Object> getMySettlementList(SettlementSearchCriteria criteria) {
        // 1. 전체 레코드 수 조회
        // countMySettlementList는 int를 반환하지만, CustomerPagination은 long을 받으므로 형변환
        long totalRecordCount = settlementMapper.countMySettlementList(criteria);

        // 2. Pagination 객체 생성 및 계산 (list는 아직 비어있음)
        // blockSize는 적절히 설정 (예: 10)
        CustomerPagination<SettlementDTO> pagination = new CustomerPagination<>(null, totalRecordCount, criteria.getCurrentPage(), criteria.getPageSize(), 10);

        // 3. 실제 정산 내역 목록 조회 (offset, pageSize 적용)
        // CustomerPagination에서 계산된 currentPage를 criteria에 다시 설정하여 정확한 offset 반영
        criteria.setCurrentPage(pagination.getCurrentPage()); // CustomerPagination 내부에서 currentPage를 보정했으므로 이를 사용
        List<SettlementDTO> settlementList = settlementMapper.selectMySettlementList(criteria);
        
        // 4. CustomerPagination 객체에 리스트 설정
        pagination.setList(settlementList); // ★★★ list 필드에 데이터 설정 ★★★

        // 5. 결과를 Map에 담아 반환
        Map<String, Object> result = new HashMap<>();
        result.put("settlementList", settlementList); // list 필드를 직접 접근하여 사용하거나, pagination.getList()로 사용 가능
        result.put("pagination", pagination);
        result.put("searchCriteria", criteria);

        return result;
    }
    
    @Override
    public String getStoreIdByUserNo(String userNo) {
        // 매퍼의 메소드를 호출하여 userNo에 해당하는 store_id를 조회합니다.
        return settlementMapper.selectStoreIdByUserNo(userNo);
    }
    
    @Override
    public List<SalesSttsDTO> getSalesHistoryByStoreId(String storeId) {
        return settlementMapper.getSalesHistoryByStoreId(storeId);
    }
    
    /**
     * 모든 상점의 전체 판매 내역을 조회하는 기능을 구현합니다.
     */
    @Override
    public List<SalesSttsDTO> getAllSalesHistory() {
        return settlementMapper.getAllSalesHistory();
    }
}
