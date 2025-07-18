package ks55team02.customer.mypage.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.customer.mypage.domain.OrderHistory;
import ks55team02.customer.mypage.mapper.OrderHistoryMapper;
import ks55team02.customer.mypage.service.OrderHistoryService;
import ks55team02.util.CustomerPagination;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderHistoryServiceImpl implements OrderHistoryService {

private final OrderHistoryMapper orderHistoryMapper;
    
    // 페이지네이션 블록 크기 (페이지 번호 1,2,3,4,5 처럼 몇 개씩 보여줄지)
    private static final int BLOCK_SIZE = 5;

    @Override
    public CustomerPagination<OrderHistory> getMyOrderHistory(String userNo, int currentPage, int pageSize) {

        // 1. 사용자의 전체 주문 건수 조회
        long totalCount = orderHistoryMapper.countMyOrders(userNo);

        // 2. 페이징 계산에 필요한 값들을 Map에 담기
        int offset = (currentPage - 1) * pageSize;
        Map<String, Object> params = new HashMap<>();
        params.put("userNo", userNo);
        params.put("size", pageSize);
        params.put("offset", offset);

        // 3. 페이징된 주문 목록 조회
        List<OrderHistory> orderList = orderHistoryMapper.findMyOrders(params);

        // 4. CustomerPagination 객체를 생성하여 최종 결과물 반환
        return new CustomerPagination<>(orderList, totalCount, currentPage, pageSize, BLOCK_SIZE);
    }
}
