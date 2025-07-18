package ks55team02.customer.mypage.service;

import ks55team02.customer.mypage.domain.OrderHistory;
import ks55team02.util.CustomerPagination;

public interface OrderHistoryService {

	/**
     * 특정 사용자의 주문 내역을 페이징하여 반환합니다.
     * @param userNo 조회할 사용자의 번호
     * @param currentPage 현재 페이지 번호
     * @param pageSize 한 페이지에 보여줄 데이터 수
     * @return 페이징 정보와 주문 목록이 담긴 CustomerPagination 객체
     */
    CustomerPagination<OrderHistory> getMyOrderHistory(String userNo, int currentPage, int pageSize);
}
